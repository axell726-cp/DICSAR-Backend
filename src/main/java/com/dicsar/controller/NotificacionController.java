package com.dicsar.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.dicsar.entity.Notificacion;
import com.dicsar.enums.TipoAlerta;
import com.dicsar.service.NotificacionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    // 🔹 Obtener todas las notificaciones
    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        List<Notificacion> notificaciones = notificacionService.listar();
        return notificaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notificaciones);
    }

    // 🔹 Obtener notificaciones paginadas
    @GetMapping("/pagina/todas")
    public ResponseEntity<Page<Notificacion>> listarPaginadas(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("fechaCreacion").descending());
        Page<Notificacion> notificaciones = notificacionService.listarPaginadas(pageable);
        return ResponseEntity.ok(notificaciones);
    }

    // 🔹 Obtener notificaciones no leídas
    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> obtenerNoLeidas() {
        List<Notificacion> notificaciones = notificacionService.listarNoLeidas();
        return notificaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notificaciones);
    }

    // 🔹 Obtener notificaciones por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Notificacion>> listarPorTipo(@PathVariable TipoAlerta tipo) {
        List<Notificacion> notificaciones = notificacionService.listarPorTipo(tipo);
        return notificaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notificaciones);
    }

    // 🔹 Obtener notificaciones por producto
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<Notificacion>> listarPorProducto(@PathVariable Long idProducto) {
        List<Notificacion> notificaciones = notificacionService.listarPorProducto(idProducto);
        return notificaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notificaciones);
    }

    // 🔹 Marcar notificación como leída
    @PostMapping("/{id}/marcar-leida")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Long id) {
        Notificacion notificacion = notificacionService.marcarComoLeida(id);
        return notificacion != null ? ResponseEntity.ok(notificacion) : ResponseEntity.notFound().build();
    }

    // 🔹 Marcar todas como leídas
    @PostMapping("/marcar-todas-leidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<String> marcarTodasComoLeidas() {
        notificacionService.marcarTodasComoLeidas();
        return ResponseEntity.ok("Todas las notificaciones marcadas como leídas");
    }

    // 🔹 Eliminar una notificación por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 🔹 Eliminar todas las notificaciones
    @DeleteMapping("/eliminar-todas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminarTodas() {
        notificacionService.eliminarTodas();
        return ResponseEntity.ok("Todas las notificaciones eliminadas");
    }

}
