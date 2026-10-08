package com.example.api_rest_aws.service;

import com.example.api_rest_aws.dto.MarcaRequestDTO;
import com.example.api_rest_aws.dto.MarcaResponseDTO;
import com.example.api_rest_aws.entity.Marca;
import com.example.api_rest_aws.exception.ConflictException;
import com.example.api_rest_aws.exception.ResourceNotFoundException;
import com.example.api_rest_aws.repository.MarcaRepository;
import com.example.api_rest_aws.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    public MarcaService(MarcaRepository marcaRepository, ProductoRepository productoRepository) {
        this.marcaRepository = marcaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public MarcaResponseDTO crearMarca(MarcaRequestDTO dto) {
        Marca marca = new Marca();
        marca.setNombreMarca(dto.getNombreMarca().trim());
        Marca guardada = marcaRepository.save(marca);
        return mapToResponseDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<MarcaResponseDTO> listarMarcas() {
        return marcaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarcaResponseDTO obtenerPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
        return mapToResponseDTO(marca);
    }

    @Transactional
    public MarcaResponseDTO actualizarMarca(Long id, MarcaRequestDTO dto) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
        marca.setNombreMarca(dto.getNombreMarca().trim());
        Marca actualizada = marcaRepository.save(marca);
        return mapToResponseDTO(actualizada);
    }

    @Transactional
    public void eliminarMarca(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Marca no encontrada con id: " + id);
        }
        if (productoRepository.existsByMarca_IdMarca(id)) {
            throw new ConflictException("No se puede eliminar la marca porque tiene productos asociados.");
        }
        marcaRepository.deleteById(id);
    }

    private MarcaResponseDTO mapToResponseDTO(Marca marca) {
        return new MarcaResponseDTO(marca.getIdMarca(), marca.getNombreMarca());
    }
}
