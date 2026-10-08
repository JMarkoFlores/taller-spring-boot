package com.example.api_rest_aws.dto;

import java.math.BigDecimal;

public class ProductoResponseDTO {

    private Long idProducto;
    private String nombreProducto;
    private Long idMarca;
    private String nombreMarca;
    private BigDecimal precioUnitario;
    private Integer stock;

    public ProductoResponseDTO() {
    }

    public ProductoResponseDTO(Long idProducto, String nombreProducto, Long idMarca, String nombreMarca, BigDecimal precioUnitario, Integer stock) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
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

    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
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
