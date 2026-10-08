package com.example.api_rest_aws.service;

import com.example.api_rest_aws.dto.ProductoRequestDTO;
import com.example.api_rest_aws.dto.ProductoResponseDTO;
import com.example.api_rest_aws.entity.Marca;
import com.example.api_rest_aws.entity.Producto;
import com.example.api_rest_aws.exception.ConflictException;
import com.example.api_rest_aws.exception.ResourceNotFoundException;
import com.example.api_rest_aws.repository.DetalleVentaRepository;
import com.example.api_rest_aws.repository.MarcaRepository;
import com.example.api_rest_aws.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           MarcaRepository marcaRepository,
                           DetalleVentaRepository detalleVentaRepository) {
        this.productoRepository = productoRepository;
        this.marcaRepository = marcaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + dto.getIdMarca()));

        Producto producto = new Producto();
        producto.setNombreProducto(dto.getNombreProducto().trim());
        producto.setMarca(marca);
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setStock(dto.getStock());

        Producto guardado = productoRepository.save(producto);
        return mapToResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return mapToResponseDTO(producto);
    }

    @Transactional
    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        Marca marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + dto.getIdMarca()));

        producto.setNombreProducto(dto.getNombreProducto().trim());
        producto.setMarca(marca);
        producto.setPrecioUnitario(dto.getPrecioUnitario());
        producto.setStock(dto.getStock());

        Producto actualizado = productoRepository.save(producto);
        return mapToResponseDTO(actualizado);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        }
        if (detalleVentaRepository.existsByProducto_IdProducto(id)) {
            throw new ConflictException("No se puede eliminar el producto porque está referenciado en una o más ventas.");
        }
        productoRepository.deleteById(id);
    }

    public ProductoResponseDTO mapToResponseDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getIdProducto(),
                producto.getNombreProducto(),
                producto.getMarca().getIdMarca(),
                producto.getMarca().getNombreMarca(),
                producto.getPrecioUnitario(),
                producto.getStock()
        );
    }
}
