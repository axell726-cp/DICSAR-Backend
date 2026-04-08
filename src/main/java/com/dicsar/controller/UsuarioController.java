package com.dicsar.controller;

import com.dicsar.dto.UsuarioDTO;
import com.dicsar.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuario(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO created = usuarioService.crearUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Integer id,
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioDTO> desactivarUsuario(@PathVariable Integer id) {
        UsuarioDTO updated = usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<UsuarioDTO> activarUsuario(@PathVariable Integer id) {
        UsuarioDTO updated = usuarioService.activarUsuario(id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/cambiar-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Void> cambiarPassword(@RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(request.getPasswordActual(), request.getPasswordNueva());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosActivos() {
        return ResponseEntity.ok(usuarioService.listarUsuariosActivos());
    }

    // Clase interna para el request
    public static class CambiarPasswordRequest {
        private String passwordActual;
        private String passwordNueva;

        public String getPasswordActual() {
            return passwordActual;
        }

        public void setPasswordActual(String passwordActual) {
            this.passwordActual = passwordActual;
        }

        public String getPasswordNueva() {
            return passwordNueva;
        }

        public void setPasswordNueva(String passwordNueva) {
            this.passwordNueva = passwordNueva;
        }
    }
}
