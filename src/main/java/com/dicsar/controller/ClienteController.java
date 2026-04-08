package com.dicsar.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;

import com.dicsar.dto.ClienteDTO;
import com.dicsar.dto.PaginatedResponse;
import com.dicsar.entity.Cliente;
import com.dicsar.service.ClienteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@Validated
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // CRUD Básico
    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return clienteService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody Cliente cliente) {
        Cliente saved = clienteService.registrar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        Cliente updated = clienteService.actualizar(id, cliente);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Búsqueda y Paginación
    @GetMapping("/pagina/todas")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.listarConPaginacion(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar/nombre")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> buscarPorNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.buscarPorNombre(nombre, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar/email")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> buscarPorEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.buscarPorEmail(email, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar/telefono")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> buscarPorTelefono(
            @RequestParam String telefono,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.buscarPorTelefono(telefono, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filtro/tipo")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> filtrarPorTipo(
            @RequestParam Boolean esEmpresa,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.listarPorTipo(esEmpresa, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filtro/estado")
    public ResponseEntity<PaginatedResponse<ClienteDTO>> filtrarPorEstado(
            @RequestParam Boolean estado,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ClienteDTO> response = clienteService.listarPorEstado(estado, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteDTO>> listarActivos() {
        List<ClienteDTO> activos = clienteService.listarClientesActivos();
        return ResponseEntity.ok(activos);
    }

    @GetMapping("/{id}/historial-compras")
    public ResponseEntity<?> obtenerHistorialCompras(@PathVariable Long id) {
        List<?> historial = clienteService.obtenerHistorialCompras(id);
        return ResponseEntity.ok(historial);
    }
}
