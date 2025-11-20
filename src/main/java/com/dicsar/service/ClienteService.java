package com.dicsar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.dicsar.dto.ClienteDTO;
import com.dicsar.dto.PaginatedResponse;
import com.dicsar.entity.Cliente;
import com.dicsar.repository.ClienteRepository;
import com.dicsar.exceptions.DuplicateResourceException;
import com.dicsar.exceptions.ResourceNotFoundException;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // CRUD básico
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente registrar(Cliente cliente) {
        if (clienteRepository.existsByNumeroDocumento(cliente.getNumeroDocumento())) {
            throw new DuplicateResourceException("El número de documento ya está registrado.");
        }
        cliente.setFechaCreacion(LocalDateTime.now());
        cliente.setFechaActualizacion(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));

        // Validar que no se duplique el documento si cambió
        if (!cliente.getNumeroDocumento().equals(clienteActualizado.getNumeroDocumento())) {
            if (clienteRepository.existsByNumeroDocumento(clienteActualizado.getNumeroDocumento())) {
                throw new DuplicateResourceException("El número de documento ya está registrado.");
            }
        }

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellidos(clienteActualizado.getApellidos());
        cliente.setRazonSocial(clienteActualizado.getRazonSocial());
        cliente.setDireccion(clienteActualizado.getDireccion());
        cliente.setTelefono(clienteActualizado.getTelefono());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setTipoDocumento(clienteActualizado.getTipoDocumento());
        cliente.setNumeroDocumento(clienteActualizado.getNumeroDocumento());
        cliente.setEsEmpresa(clienteActualizado.getEsEmpresa());
        cliente.setEstado(clienteActualizado.getEstado());
        cliente.setFechaActualizacion(LocalDateTime.now());

        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));

        // Borrado lógico: marcar como inactivo en lugar de eliminar físicamente
        cliente.setEstado(false);
        cliente.setFechaActualizacion(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public List<?> obtenerHistorialCompras(Long idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new ResourceNotFoundException("Cliente no encontrado.");
        }
        // Método placeholder: se conectará con ReporteVenta cuando sea necesario
        return List.of();
    }

    // Métodos de búsqueda con paginación
    public PaginatedResponse<ClienteDTO> buscarPorNombre(String nombre, int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.buscarPorNombre(nombre, pageable);
        return convertPageToResponse(page);
    }

    public PaginatedResponse<ClienteDTO> buscarPorEmail(String email, int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.findByEmailContainingIgnoreCase(email, pageable);
        return convertPageToResponse(page);
    }

    public PaginatedResponse<ClienteDTO> buscarPorTelefono(String telefono, int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.findByTelefonoContainingIgnoreCase(telefono, pageable);
        return convertPageToResponse(page);
    }

    public PaginatedResponse<ClienteDTO> listarConPaginacion(int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.findAll(pageable);
        return convertPageToResponse(page);
    }

    public PaginatedResponse<ClienteDTO> listarPorTipo(Boolean esEmpresa, int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.findByEsEmpresa(esEmpresa, pageable);
        return convertPageToResponse(page);
    }

    public PaginatedResponse<ClienteDTO> listarPorEstado(Boolean estado, int pageNumber, int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        Page<Cliente> page = clienteRepository.findByEstado(estado, pageable);
        return convertPageToResponse(page);
    }

    public List<ClienteDTO> listarClientesActivos() {
        return clienteRepository.findByEstadoTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Métodos auxiliares
    private ClienteDTO convertToDTO(Cliente cliente) {
        return ClienteDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .apellidos(cliente.getApellidos())
                .tipoDocumento(cliente.getTipoDocumento())
                .numeroDocumento(cliente.getNumeroDocumento())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .razonSocial(cliente.getRazonSocial())
                .esEmpresa(cliente.getEsEmpresa())
                .estado(cliente.getEstado())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .build();
    }

    private PaginatedResponse<ClienteDTO> convertPageToResponse(Page<Cliente> page) {
        return PaginatedResponse.<ClienteDTO>builder()
                .content(page.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }

    // Métodos de conteo para dashboard
    public Long countClientesActivos() {
        return clienteRepository.countByEstadoTrue();
    }
}
