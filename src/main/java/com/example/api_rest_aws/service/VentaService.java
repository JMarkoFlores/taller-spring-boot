package com.example.api_rest_aws.service;

import com.example.api_rest_aws.dto.DetalleVentaRequestDTO;
import com.example.api_rest_aws.dto.DetalleVentaResponseDTO;
import com.example.api_rest_aws.dto.VentaRequestDTO;
import com.example.api_rest_aws.dto.VentaResponseDTO;
import com.example.api_rest_aws.entity.Cliente;
import com.example.api_rest_aws.entity.DetalleVenta;
import com.example.api_rest_aws.entity.Producto;
import com.example.api_rest_aws.entity.Venta;
import com.example.api_rest_aws.exception.BadRequestException;
import com.example.api_rest_aws.exception.InsufficientStockException;
import com.example.api_rest_aws.exception.ResourceNotFoundException;
import com.example.api_rest_aws.repository.ClienteRepository;
import com.example.api_rest_aws.repository.ProductoRepository;
import com.example.api_rest_aws.repository.VentaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository,
                        ClienteRepository clienteRepository,
                        ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Registra una venta, descuenta stock de los productos y almacena detalles
     * de forma atómica (@Transactional) con bloqueo pesimista contra concurrencia.
     */
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        validarDetallesNoVacios(request.getDetalles());
        validarSinProductosDuplicados(request.getDetalles());

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getIdCliente()));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFechaVenta(LocalDateTime.now()); // Fecha generada por el servidor

        // Ordenamos los IDs para adquirirlos en orden consistente y prevenir deadlocks entre transacciones
        Set<Long> productIdsOrdenados = new TreeSet<>();
        for (DetalleVentaRequestDTO detalle : request.getDetalles()) {
            productIdsOrdenados.add(detalle.getIdProducto());
        }

        // Adquirimos bloqueo pesimista (SELECT ... FOR UPDATE) para cada producto
        Map<Long, Producto> productosBloqueados = new HashMap<>();
        for (Long prodId : productIdsOrdenados) {
            Producto producto = productoRepository.findByIdForUpdate(prodId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + prodId));
            productosBloqueados.put(prodId, producto);
        }

        // Verificamos stock suficiente y descontamos
        for (DetalleVentaRequestDTO item : request.getDetalles()) {
            Producto prod = productosBloqueados.get(item.getIdProducto());
            if (prod.getStock() < item.getCantidadVendido()) {
                throw new InsufficientStockException(String.format(
                        "Stock insuficiente para el producto '%s' (ID: %d). Stock disponible: %d, solicitado: %d.",
                        prod.getNombreProducto(), prod.getIdProducto(), prod.getStock(), item.getCantidadVendido()
                ));
            }

            prod.setStock(prod.getStock() - item.getCantidadVendido());
            productoRepository.save(prod);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(prod);
            detalle.setCantidadVendido(item.getCantidadVendido());
            venta.addDetalle(detalle);
        }

        Venta ventaGuardada = ventaRepository.save(venta);
        return mapToResponseDTO(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarVentas() {
        return ventaRepository.findAllWithDetails().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        return mapToResponseDTO(venta);
    }

    /**
     * Actualiza una venta ajustando el stock según las cantidades previas y las nuevas,
     * todo dentro de la misma transacción y con bloqueo pesimista.
     */
    @Transactional
    public VentaResponseDTO actualizarVenta(Long idVenta, VentaRequestDTO request) {
        validarDetallesNoVacios(request.getDetalles());
        validarSinProductosDuplicados(request.getDetalles());

        Venta venta = ventaRepository.findByIdWithDetails(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + idVenta));

        if (!venta.getCliente().getIdCliente().equals(request.getIdCliente())) {
            Cliente nuevoCliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getIdCliente()));
            venta.setCliente(nuevoCliente);
        }

        // Obtener la unión ordenada de productos involucrados (anteriores y nuevos)
        Set<Long> productIdsAfectados = new TreeSet<>();
        for (DetalleVenta oldDetalle : venta.getDetalles()) {
            productIdsAfectados.add(oldDetalle.getProducto().getIdProducto());
        }
        for (DetalleVentaRequestDTO newDetalle : request.getDetalles()) {
            productIdsAfectados.add(newDetalle.getIdProducto());
        }

        // Bloquear todos los productos afectados en orden consistente
        Map<Long, Producto> productosBloqueados = new HashMap<>();
        for (Long pId : productIdsAfectados) {
            Producto prod = productoRepository.findByIdForUpdate(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + pId));
            productosBloqueados.put(pId, prod);
        }

        // 1. Revertir cantidades anteriores al inventario
        for (DetalleVenta oldDetalle : venta.getDetalles()) {
            Producto prod = productosBloqueados.get(oldDetalle.getProducto().getIdProducto());
            prod.setStock(prod.getStock() + oldDetalle.getCantidadVendido());
        }

        // 2. Validar que las nuevas cantidades no sobrepasen el stock disponible y descontar
        for (DetalleVentaRequestDTO newDetalle : request.getDetalles()) {
            Producto prod = productosBloqueados.get(newDetalle.getIdProducto());
            if (prod.getStock() < newDetalle.getCantidadVendido()) {
                throw new InsufficientStockException(String.format(
                        "Stock insuficiente para el producto '%s' (ID: %d) tras ajuste. Stock disponible: %d, solicitado: %d.",
                        prod.getNombreProducto(), prod.getIdProducto(), prod.getStock(), newDetalle.getCantidadVendido()
                ));
            }
            prod.setStock(prod.getStock() - newDetalle.getCantidadVendido());
        }

        // Guardar cambios en productos
        for (Producto prod : productosBloqueados.values()) {
            productoRepository.save(prod);
        }

        // 3. Reemplazar los detalles de la venta
        venta.getDetalles().clear();
        for (DetalleVentaRequestDTO newDetalle : request.getDetalles()) {
            Producto prod = productosBloqueados.get(newDetalle.getIdProducto());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(prod);
            detalle.setCantidadVendido(newDetalle.getCantidadVendido());
            venta.addDetalle(detalle);
        }

        Venta ventaActualizada = ventaRepository.save(venta);
        return mapToResponseDTO(ventaActualizada);
    }

    /**
     * Elimina una venta devolviendo las cantidades vendidas al stock de cada producto.
     */
    @Transactional
    public void eliminarVenta(Long idVenta) {
        Venta venta = ventaRepository.findByIdWithDetails(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + idVenta));

        // Obtener IDs ordenados de productos a devolver
        Set<Long> productIds = new TreeSet<>();
        for (DetalleVenta dv : venta.getDetalles()) {
            productIds.add(dv.getProducto().getIdProducto());
        }

        for (Long pId : productIds) {
            Producto prod = productoRepository.findByIdForUpdate(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + pId));

            int cantidadARestituir = venta.getDetalles().stream()
                    .filter(d -> d.getProducto().getIdProducto().equals(pId))
                    .mapToInt(DetalleVenta::getCantidadVendido)
                    .sum();

            prod.setStock(prod.getStock() + cantidadARestituir);
            productoRepository.save(prod);
        }

        ventaRepository.delete(venta);
    }

    private void validarDetallesNoVacios(List<DetalleVentaRequestDTO> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new BadRequestException("La venta debe tener al menos un detalle de producto.");
        }
    }

    private void validarSinProductosDuplicados(List<DetalleVentaRequestDTO> detalles) {
        Set<Long> vistos = new HashSet<>();
        for (DetalleVentaRequestDTO detalle : detalles) {
            if (!vistos.add(detalle.getIdProducto())) {
                throw new BadRequestException("No se permite repetir el mismo producto (ID: " + detalle.getIdProducto() + ") en una misma venta.");
            }
        }
    }

    private VentaResponseDTO mapToResponseDTO(Venta venta) {
        List<DetalleVentaResponseDTO> detallesDTO = new ArrayList<>();
        if (venta.getDetalles() != null) {
            for (DetalleVenta d : venta.getDetalles()) {
                detallesDTO.add(new DetalleVentaResponseDTO(
                        d.getIdDetalle(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombreProducto(),
                        d.getCantidadVendido()
                ));
            }
        }

        String nombreCompletoCliente = (venta.getCliente() != null)
                ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido()
                : "Desconocido";

        Long clienteId = (venta.getCliente() != null) ? venta.getCliente().getIdCliente() : null;

        return new VentaResponseDTO(
                venta.getIdVenta(),
                clienteId,
                nombreCompletoCliente,
                venta.getFechaVenta(),
                detallesDTO
        );
    }
}
