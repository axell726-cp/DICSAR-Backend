package com.dicsar.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.dicsar.entity.UnidadMed;
import com.dicsar.service.UnidadMedService;

@RestController
@RequestMapping("/api/unidades-medida")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
public class UnidadMedController {

    private final UnidadMedService unidadMedService;

    public UnidadMedController(UnidadMedService unidadMedService) {
        this.unidadMedService = unidadMedService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public List<UnidadMed> listar() {
        return unidadMedService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<UnidadMed> obtener(@PathVariable Long id) {
        return unidadMedService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadMed> guardar(@Valid @RequestBody UnidadMed unidadMed) {
        UnidadMed guardada = unidadMedService.guardar(unidadMed);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadMed> actualizar(@PathVariable Long id, @Valid @RequestBody UnidadMed unidadMed) {
        UnidadMed actualizada = unidadMedService.actualizar(id, unidadMed);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<UnidadMed> existente = unidadMedService.obtener(id);

        if (existente.isPresent()) {
            unidadMedService.eliminar(id);
            return ResponseEntity.ok("Unidad de medida eliminada correctamente.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
