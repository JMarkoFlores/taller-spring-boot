package com.example.api_rest_aws.dto;

public class DetalleVentaResponseDTO {

    private Long idDetalle;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidadVendido;

    public DetalleVentaResponseDTO() {
    }

    public DetalleVentaResponseDTO(Long idDetalle, Long idProducto, String nombreProducto, Integer cantidadVendido) {
        this.idDetalle = idDetalle;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidadVendido = cantidadVendido;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidadVendido() {
        return cantidadVendido;
    }

    public void setCantidadVendido(Integer cantidadVendido) {
        this.cantidadVendido = cantidadVendido;
    }
}
