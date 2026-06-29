package com.dicsar.service;

import com.dicsar.entity.RolEntity;
import com.dicsar.entity.AuditLog;
import com.dicsar.repository.AuditLogRepository;
import com.dicsar.repository.RolRepository;
import com.dicsar.exceptions.DuplicateResourceException;
import com.dicsar.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<RolEntity> listarTodos() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RolEntity> listarActivos() {
        return rolRepository.findByActivo(true);
    }

    @Transactional(readOnly = true)
    public Optional<RolEntity> obtenerPorId(Integer id) {
        return rolRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RolEntity> obtenerPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    @Transactional
    public RolEntity crear(RolEntity rol) {
        // Validar que el nombre sea único
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new DuplicateResourceException("El rol '" + rol.getNombre() + "' ya existe.");
        }

        // Validar que el nombre no esté vacío
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es requerido.");
        }

        // Si no se especifica activo, por defecto es true
        if (rol.getActivo() == null) {
            rol.setActivo(true);
        }

        rol.setFechaCreacion(LocalDateTime.now());
        rol.setFechaActualizacion(LocalDateTime.now());

        RolEntity saved = rolRepository.save(rol);
        logCambio("CREAR", "Creado rol id=" + saved.getIdRol() + " nombre=" + saved.getNombre());
        return saved;
    }

    @Transactional
    public RolEntity actualizar(Integer id, RolEntity rolActualizado) {
        RolEntity rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));

        // Validar que el nombre sea único si cambió
        if (rolActualizado.getNombre() != null && !rolActualizado.getNombre().equals(rol.getNombre())) {
            if (rolRepository.existsByNombre(rolActualizado.getNombre())) {
                throw new DuplicateResourceException("El rol '" + rolActualizado.getNombre() + "' ya existe.");
            }
            rol.setNombre(rolActualizado.getNombre());
        }

        if (rolActualizado.getDescripcion() != null) {
            rol.setDescripcion(rolActualizado.getDescripcion());
        }

        if (rolActualizado.getActivo() != null) {
            rol.setActivo(rolActualizado.getActivo());
        }

        if (rolActualizado.getPermisos() != null) {
            rol.setPermisos(rolActualizado.getPermisos());
        }

        rol.setFechaActualizacion(LocalDateTime.now());

        RolEntity saved = rolRepository.save(rol);
        logCambio("ACTUALIZAR", "Actualizado rol id=" + saved.getIdRol() + " nombre=" + saved.getNombre());
        return saved;
    }

    @Transactional
    public void eliminar(Integer id) {
        RolEntity rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado."));

        // Validar que no sea un rol del sistema (ADMIN o VENDEDOR)
        String nombre = rol.getNombre().toUpperCase();
        if (nombre.equals("ADMIN") || nombre.equals("VENDEDOR")) {
            throw new IllegalArgumentException("No se puede eliminar un rol del sistema.");
        }

        rolRepository.deleteById(id);
        logCambio("ELIMINAR", "Eliminado rol id=" + id + " nombre=" + rol.getNombre());
    }

    private void logCambio(String accion, String detalle) {
        try {
            AuditLog a = new AuditLog();
            a.setEntidad("Rol");
            a.setAccion(accion);
            String usuario = "unknown";
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) usuario = auth.getName();
            } catch (Exception ignored) {}
            a.setUsuario(usuario);
            a.setFecha(LocalDateTime.now());
            a.setDetalle(detalle);
            auditLogRepository.save(a);
        } catch (Exception ex) {
            // No interrumpir la operación principal por fallas en auditoría
        }
    }
}
