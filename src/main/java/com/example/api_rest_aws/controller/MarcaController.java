package com.example.api_rest_aws.controller;

import com.example.api_rest_aws.dto.MarcaRequestDTO;
import com.example.api_rest_aws.dto.MarcaResponseDTO;
import com.example.api_rest_aws.service.MarcaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping
    public ResponseEntity<MarcaResponseDTO> crearMarca(@Valid @RequestBody MarcaRequestDTO dto) {
        MarcaResponseDTO creada = marcaService.crearMarca(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public ResponseEntity<List<MarcaResponseDTO>> listarMarcas() {
        return ResponseEntity.ok(marcaService.listarMarcas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> obtenerMarca(@PathVariable Long id) {
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> actualizarMarca(@PathVariable Long id, @Valid @RequestBody MarcaRequestDTO dto) {
        return ResponseEntity.ok(marcaService.actualizarMarca(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMarca(@PathVariable Long id) {
        marcaService.eliminarMarca(id);
        return ResponseEntity.noContent().build();
    }
}
