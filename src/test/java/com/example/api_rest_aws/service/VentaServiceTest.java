package com.example.api_rest_aws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api_rest_aws.dto.DetalleVentaRequestDTO;
import com.example.api_rest_aws.dto.VentaRequestDTO;
import com.example.api_rest_aws.dto.VentaResponseDTO;
import com.example.api_rest_aws.entity.Cliente;
import com.example.api_rest_aws.entity.DetalleVenta;
import com.example.api_rest_aws.entity.Marca;
import com.example.api_rest_aws.entity.Producto;
import com.example.api_rest_aws.entity.Venta;
import com.example.api_rest_aws.exception.BadRequestException;
import com.example.api_rest_aws.exception.ConflictException;
import com.example.api_rest_aws.exception.InsufficientStockException;
import com.example.api_rest_aws.exception.ResourceNotFoundException;
import com.example.api_rest_aws.repository.ClienteRepository;
import com.example.api_rest_aws.repository.DetalleVentaRepository;
import com.example.api_rest_aws.repository.MarcaRepository;
import com.example.api_rest_aws.repository.ProductoRepository;
import com.example.api_rest_aws.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private VentaService ventaService;

    private Cliente clienteEjemplo;
    private Marca marcaEjemplo;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        clienteEjemplo = new Cliente(1L, "Juan", "Perez");
        marcaEjemplo = new Marca(10L, "Sony");
        producto1 = new Producto(101L, "Audifonos", marcaEjemplo, new BigDecimal("50.00"), 10);
        producto2 = new Producto(102L, "Parlante", marcaEjemplo, new BigDecimal("120.00"), 5);
    }

    @Test
    @DisplayName("Debe crear venta exitosamente y descontar el stock de los productos")
    void crearVenta_Exito_DescuentaStock() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setIdVenta(1001L);
            return v;
        });

        List<DetalleVentaRequestDTO> detalles = List.of(
                new DetalleVentaRequestDTO(101L, 3),
                new DetalleVentaRequestDTO(102L, 2)
        );
        VentaRequestDTO request = new VentaRequestDTO(1L, detalles);

        VentaResponseDTO respuesta = ventaService.crearVenta(request);

        assertNotNull(respuesta);
        assertEquals(1001L, respuesta.getIdVenta());
        assertEquals("Juan Perez", respuesta.getNombreCliente());
        assertEquals(7, producto1.getStock(), "El stock de producto 1 debe haber bajado de 10 a 7");
        assertEquals(3, producto2.getStock(), "El stock de producto 2 debe haber bajado de 5 a 3");
        verify(productoRepository, times(1)).save(producto1);
        verify(productoRepository, times(1)).save(producto2);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe fallar al crear venta si el stock es insuficiente")
    void crearVenta_StockInsuficiente_LanzaExcepcion() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteEjemplo));
        when(productoRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(producto1));

        List<DetalleVentaRequestDTO> detalles = List.of(
                new DetalleVentaRequestDTO(101L, 15) // Pide 15 pero solo hay 10
        );
        VentaRequestDTO request = new VentaRequestDTO(1L, detalles);

        assertThrows(InsufficientStockException.class, () -> ventaService.crearVenta(request));
        assertEquals(10, producto1.getStock(), "El stock no debe modificarse si la validación falla");
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe fallar si se repite el mismo producto en la solicitud de venta")
    void crearVenta_ProductoDuplicado_LanzaBadRequestException() {
        List<DetalleVentaRequestDTO> detalles = List.of(
                new DetalleVentaRequestDTO(101L, 2),
                new DetalleVentaRequestDTO(101L, 3)
        );
        VentaRequestDTO request = new VentaRequestDTO(1L, detalles);

        assertThrows(BadRequestException.class, () -> ventaService.crearVenta(request));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe fallar si la lista de detalles está vacía")
    void crearVenta_DetallesVacios_LanzaBadRequestException() {
        VentaRequestDTO request = new VentaRequestDTO(1L, List.of());
        assertThrows(BadRequestException.class, () -> ventaService.crearVenta(request));
    }

    @Test
    @DisplayName("Debe actualizar venta y ajustar stock devolviendo cantidades previas y aplicando nuevas")
    void actualizarVenta_AjustaStockCorrectamente() {
        Venta ventaExistente = new Venta(500L, clienteEjemplo, LocalDateTime.now().minusDays(1));
        DetalleVenta detalleExistente = new DetalleVenta(1L, ventaExistente, producto1, 4); // ya consumió 4
        ventaExistente.setDetalles(new ArrayList<>(List.of(detalleExistente)));

        // Stock actual en base de datos es 6 (10 inicial - 4 vendidos)
        producto1.setStock(6);

        when(ventaRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(ventaExistente));
        when(productoRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(producto1));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Actualizamos la venta para pedir solo 2 unidades en vez de 4 (debe devolver 2 al inventario)
        List<DetalleVentaRequestDTO> nuevosDetalles = List.of(
                new DetalleVentaRequestDTO(101L, 2)
        );
        VentaRequestDTO request = new VentaRequestDTO(1L, nuevosDetalles);

        VentaResponseDTO respuesta = ventaService.actualizarVenta(500L, request);

        assertNotNull(respuesta);
        assertEquals(8, producto1.getStock(), "Stock debe ser 6 + 4 (revertido) - 2 (nuevo pedido) = 8");
        verify(ventaRepository, times(1)).save(ventaExistente);
    }

    @Test
    @DisplayName("Debe eliminar venta y devolver todas las cantidades vendidas al stock")
    void eliminarVenta_DevuelveStock() {
        Venta ventaExistente = new Venta(700L, clienteEjemplo, LocalDateTime.now());
        DetalleVenta d1 = new DetalleVenta(10L, ventaExistente, producto1, 3);
        DetalleVenta d2 = new DetalleVenta(20L, ventaExistente, producto2, 2);
        ventaExistente.setDetalles(new ArrayList<>(List.of(d1, d2)));

        producto1.setStock(7);
        producto2.setStock(3);

        when(ventaRepository.findByIdWithDetails(700L)).thenReturn(Optional.of(ventaExistente));
        when(productoRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(producto2));

        ventaService.eliminarVenta(700L);

        assertEquals(10, producto1.getStock(), "Debe restituir 3 unidades (7 + 3 = 10)");
        assertEquals(5, producto2.getStock(), "Debe restituir 2 unidades (3 + 2 = 5)");
        verify(ventaRepository, times(1)).delete(ventaExistente);
    }
}
