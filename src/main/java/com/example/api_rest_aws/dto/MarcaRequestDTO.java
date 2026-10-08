package com.example.api_rest_aws.dto;

import jakarta.validation.constraints.NotBlank;

public class MarcaRequestDTO {

    @NotBlank(message = "El nombre de la marca es obligatorio")
    private String nombreMarca;

    public MarcaRequestDTO() {
    }

    public MarcaRequestDTO(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }
}
