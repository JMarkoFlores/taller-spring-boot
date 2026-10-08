package com.example.api_rest_aws.dto;

public class MarcaResponseDTO {

    private Long idMarca;
    private String nombreMarca;

    public MarcaResponseDTO() {
    }

    public MarcaResponseDTO(Long idMarca, String nombreMarca) {
        this.idMarca = idMarca;
        this.nombreMarca = nombreMarca;
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
}
