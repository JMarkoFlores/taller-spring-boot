package com.example.api_rest_aws.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api_rest_aws.exception.ConflictException;
import com.example.api_rest_aws.repository.ClienteRepository;
import com.example.api_rest_aws.repository.DetalleVentaRepository;
import com.example.api_rest_aws.repository.MarcaRepository;
import com.example.api_rest_aws.repository.ProductoRepository;
import com.example.api_rest_aws.repository.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityConflictDeleteTest {

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private MarcaService marcaService;

    @InjectMocks
    private ProductoService productoService;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("No debe permitir eliminar una marca si tiene productos asociados")
    void eliminarMarca_ConProductosAsociados_LanzaConflictException() {
        when(marcaRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.existsByMarca_IdMarca(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> marcaService.eliminarMarca(1L));
        verify(marcaRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("No debe permitir eliminar un cliente si tiene ventas asociadas")
    void eliminarCliente_ConVentasAsociadas_LanzaConflictException() {
        when(clienteRepository.existsById(2L)).thenReturn(true);
        when(ventaRepository.existsByCliente_IdCliente(2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> clienteService.eliminarCliente(2L));
        verify(clienteRepository, never()).deleteById(2L);
    }

    @Test
    @DisplayName("No debe permitir eliminar un producto si está referenciado en ventas")
    void eliminarProducto_ConVentasAsociadas_LanzaConflictException() {
        when(productoRepository.existsById(3L)).thenReturn(true);
        when(detalleVentaRepository.existsByProducto_IdProducto(3L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> productoService.eliminarProducto(3L));
        verify(productoRepository, never()).deleteById(3L);
    }
}
