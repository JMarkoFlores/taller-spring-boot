package com.example.api_rest_aws.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaResponseDTO {

    private Long idVenta;
    private Long idCliente;
    private String nombreCliente;
    private LocalDateTime fechaVenta;
    private List<DetalleVentaResponseDTO> detalles = new ArrayList<>();

    public VentaResponseDTO() {
    }

    public VentaResponseDTO(Long idVenta, Long idCliente, String nombreCliente, LocalDateTime fechaVenta, List<DetalleVentaResponseDTO> detalles) {
        this.idVenta = idVenta;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.fechaVenta = fechaVenta;
        this.detalles = detalles;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public Long getClienteId() {
        return idCliente;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public List<DetalleVentaResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
