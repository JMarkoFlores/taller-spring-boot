package com.example.api_rest_aws.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    @NotNull(message = "El identificador de la marca es obligatorio")
    private Long idMarca;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio unitario debe ser mayor que cero")
    private BigDecimal precioUnitario;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual que cero")
    private Integer stock;

    public ProductoRequestDTO() {
    }

    public ProductoRequestDTO(String nombreProducto, Long idMarca, BigDecimal precioUnitario, Integer stock) {
        this.nombreProducto = nombreProducto;
        this.idMarca = idMarca;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
