package com.example.api_rest_aws.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class VentaRequestDTO {

    @NotNull(message = "El identificador del cliente es obligatorio")
    private Long idCliente;

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<DetalleVentaRequestDTO> detalles = new ArrayList<>();

    public VentaRequestDTO() {
    }

    public VentaRequestDTO(Long idCliente, List<DetalleVentaRequestDTO> detalles) {
        this.idCliente = idCliente;
        this.detalles = detalles;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public List<DetalleVentaRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaRequestDTO> detalles) {
        this.detalles = detalles;
    }
}
