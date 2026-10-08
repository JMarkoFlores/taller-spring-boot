package com.example.api_rest_aws.service;

import com.example.api_rest_aws.dto.ClienteRequestDTO;
import com.example.api_rest_aws.dto.ClienteResponseDTO;
import com.example.api_rest_aws.entity.Cliente;
import com.example.api_rest_aws.exception.ConflictException;
import com.example.api_rest_aws.exception.ResourceNotFoundException;
import com.example.api_rest_aws.repository.ClienteRepository;
import com.example.api_rest_aws.repository.VentaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    public ClienteService(ClienteRepository clienteRepository, VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre().trim());
        cliente.setApellido(dto.getApellido().trim());
        Cliente guardado = clienteRepository.save(cliente);
        return mapToResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return mapToResponseDTO(cliente);
    }

    @Transactional
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        cliente.setNombre(dto.getNombre().trim());
        cliente.setApellido(dto.getApellido().trim());
        Cliente actualizado = clienteRepository.save(cliente);
        return mapToResponseDTO(actualizado);
    }

    @Transactional
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        if (ventaRepository.existsByCliente_IdCliente(id)) {
            throw new ConflictException("No se puede eliminar el cliente porque tiene ventas asociadas.");
        }
        clienteRepository.deleteById(id);
    }

    public ClienteResponseDTO mapToResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(cliente.getIdCliente(), cliente.getNombre(), cliente.getApellido());
    }
}
