package com.example.api_rest_aws.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VentaIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    private static Long marcaId;
    private static Long productoId;
    private static Long clienteId;
    private static Long ventaId;

    @Test
    @Order(1)
    @DisplayName("1. Crear marca exitosamente (POST 201)")
    void testCrearMarca() throws Exception {
        String json = """
            {
                "nombreMarca": "Logitech"
            }
            """;

        String response = mockMvc.perform(post("/api/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMarca").isNumber())
                .andExpect(jsonPath("$.nombreMarca").value("Logitech"))
                .andReturn().getResponse().getContentAsString();

        // Extraer id creado de forma sencilla
        marcaId = extractId(response, "idMarca");
    }

    @Test
    @Order(2)
    @DisplayName("2. Crear producto asociado a la marca (POST 201)")
    void testCrearProducto() throws Exception {
        String json = String.format("""
            {
                "nombreProducto": "Mouse MX Master 3",
                "idMarca": %d,
                "precioUnitario": 99.99,
                "stock": 10
            }
            """, marcaId);

        String response = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").isNumber())
                .andExpect(jsonPath("$.nombreProducto").value("Mouse MX Master 3"))
                .andExpect(jsonPath("$.stock").value(10))
                .andReturn().getResponse().getContentAsString();

        productoId = extractId(response, "idProducto");
    }

    @Test
    @Order(3)
    @DisplayName("3. Crear cliente (POST 201)")
    void testCrearCliente() throws Exception {
        String json = """
            {
                "nombre": "Carlos",
                "apellido": "Gomez"
            }
            """;

        String response = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCliente").isNumber())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andReturn().getResponse().getContentAsString();

        clienteId = extractId(response, "idCliente");
    }

    @Test
    @Order(4)
    @DisplayName("4. Registrar venta y verificar descuento de stock (POST 201)")
    void testRegistrarVenta() throws Exception {
        String json = String.format("""
            {
                "idCliente": %d,
                "detalles": [
                    {
                        "idProducto": %d,
                        "cantidadVendido": 4
                    }
                ]
            }
            """, clienteId, productoId);

        String response = mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVenta").isNumber())
                .andExpect(jsonPath("$.fechaVenta").isNotEmpty())
                .andExpect(jsonPath("$.detalles[0].cantidadVendido").value(4))
                .andReturn().getResponse().getContentAsString();

        ventaId = extractId(response, "idVenta");

        // Verificar que el stock bajó de 10 a 6
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(6));
    }

    @Test
    @Order(5)
    @DisplayName("5. Fallar al comprar más de lo disponible (POST 409 Conflict)")
    void testVentaStockInsuficiente() throws Exception {
        // Stock actual es 6, intentamos comprar 10
        String json = String.format("""
            {
                "idCliente": %d,
                "detalles": [
                    {
                        "idProducto": %d,
                        "cantidadVendido": 10
                    }
                ]
            }
            """, clienteId, productoId);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));

        // Verificar que el stock sigue en 6
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(6));
    }

    @Test
    @Order(6)
    @DisplayName("6. Fallar al repetir producto en la misma solicitud (POST 400 Bad Request)")
    void testVentaProductoDuplicado() throws Exception {
        String json = String.format("""
            {
                "idCliente": %d,
                "detalles": [
                    {
                        "idProducto": %d,
                        "cantidadVendido": 1
                    },
                    {
                        "idProducto": %d,
                        "cantidadVendido": 2
                    }
                ]
            }
            """, clienteId, productoId, productoId);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("No se permite repetir el mismo producto")));
    }

    @Test
    @Order(7)
    @DisplayName("7. Rechazar eliminación de marca con productos asociados (DELETE 409)")
    void testEliminarMarcaConProductos() throws Exception {
        mockMvc.perform(delete("/api/marcas/" + marcaId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("tiene productos asociados")));
    }

    @Test
    @Order(8)
    @DisplayName("8. Rechazar eliminación de cliente con ventas asociadas (DELETE 409)")
    void testEliminarClienteConVentas() throws Exception {
        mockMvc.perform(delete("/api/clientes/" + clienteId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("tiene ventas asociadas")));
    }

    @Test
    @Order(9)
    @DisplayName("9. Rechazar eliminación de producto referenciado en ventas (DELETE 409)")
    void testEliminarProductoConVentas() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("referenciado en una o más ventas")));
    }

    @Test
    @Order(10)
    @DisplayName("10. Eliminar venta y devolver stock al producto (DELETE 204)")
    void testEliminarVentaYDevolverStock() throws Exception {
        mockMvc.perform(delete("/api/ventas/" + ventaId))
                .andExpect(status().isNoContent());

        // Stock debe haber vuelto a 10 (6 + 4)
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @Order(11)
    @DisplayName("11. Limpieza de datos: eliminar producto, cliente y marca ya sin dependencias (DELETE 204)")
    void testLimpiezaFinal() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/clientes/" + clienteId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/marcas/" + marcaId))
                .andExpect(status().isNoContent());
    }

    private Long extractId(String jsonResponse, String idFieldName) {
        String target = "\"" + idFieldName + "\":";
        int idx = jsonResponse.indexOf(target);
        if (idx == -1) return null;
        int start = idx + target.length();
        int end = jsonResponse.indexOf(",", start);
        if (end == -1) {
            end = jsonResponse.indexOf("}", start);
        }
        return Long.parseLong(jsonResponse.substring(start, end).trim());
    }
}
