package com.dicsar.controller;

import com.dicsar.entity.RolEntity;
import com.dicsar.service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    /**
     * GET /api/roles - Listar todos los roles
     */
    @GetMapping
    public ResponseEntity<List<RolEntity>> listarTodos() {
        List<RolEntity> roles = rolService.listarTodos();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /api/roles/activos - Listar solo roles activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<RolEntity>> listarActivos() {
        List<RolEntity> roles = rolService.listarActivos();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /api/roles/{id} - Obtener rol por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolEntity> obtenerPorId(@PathVariable Integer id) {
        Optional<RolEntity> rol = rolService.obtenerPorId(id);
        return rol.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/roles - Crear nuevo rol
     * Request body:
     * {
     * "nombre": "GERENTE",
     * "descripcion": "Gerente de sucursal",
     * "activo": true
     * }
     */
    @PostMapping
    public ResponseEntity<RolEntity> crear(@Valid @RequestBody RolEntity rol) {
        RolEntity created = rolService.crear(rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/roles/{id} - Actualizar rol
     * Request body:
     * {
     * "nombre": "GERENTE",
     * "descripcion": "Gerente de sucursal actualizado",
     * "activo": true
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<RolEntity> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody RolEntity rol) {
        RolEntity updated = rolService.actualizar(id, rol);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/roles/{id} - Eliminar rol
     * No se pueden eliminar roles del sistema (ADMIN, VENDEDOR)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
