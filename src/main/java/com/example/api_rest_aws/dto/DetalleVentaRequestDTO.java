package com.example.api_rest_aws.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DetalleVentaRequestDTO {

    @NotNull(message = "El identificador del producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "La cantidad vendida es obligatoria")
    @Min(value = 1, message = "La cantidad vendida debe ser mayor que cero")
    private Integer cantidadVendido;

    public DetalleVentaRequestDTO() {
    }

    public DetalleVentaRequestDTO(Long idProducto, Integer cantidadVendido) {
        this.idProducto = idProducto;
        this.cantidadVendido = cantidadVendido;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadVendido() {
        return cantidadVendido;
    }

    public void setCantidadVendido(Integer cantidadVendido) {
        this.cantidadVendido = cantidadVendido;
    }
}
