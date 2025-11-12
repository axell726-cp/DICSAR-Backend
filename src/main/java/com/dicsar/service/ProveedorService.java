package com.dicsar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dicsar.entity.Proveedor;
import com.dicsar.exceptions.DuplicateResourceException;
import com.dicsar.exceptions.ResourceNotFoundException;
import com.dicsar.repository.ProveedorRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    public Page<Proveedor> listarPaginado(String buscar, Boolean estado, Pageable pageable) {
        if (estado != null) {
            return proveedorRepository.findByEstado(estado, pageable);
        }
        return proveedorRepository.buscarProveedores(buscar, pageable);
    }

    public Page<Proveedor> buscarProveedoresActivos(String buscar, Pageable pageable) {
        return proveedorRepository.buscarProveedoresActivos(buscar, pageable);
    }

    public Optional<Proveedor> obtener(Long id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor guardar(Proveedor proveedor) {
        validarProveedor(proveedor);

        if (proveedor.getIdProveedor() == null) {
            proveedor.setFechaCreacion(LocalDateTime.now());
            proveedor.setEstado(true);
        } else {
            proveedor.setFechaActualizacion(LocalDateTime.now());
        }

        return proveedorRepository.save(proveedor);
    }

    public void eliminar(Long id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isEmpty()) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + id);
        }

        Proveedor proveedor = proveedorOpt.get();
        if (Boolean.TRUE.equals(proveedor.getEstado())) {
            throw new IllegalArgumentException("No se puede eliminar un proveedor activo. Desactívelo primero.");
        }

        proveedorRepository.deleteById(id);
    }

    private void validarProveedor(Proveedor proveedor) {
        // Validar RUC obligatorio
        if (!StringUtils.hasText(proveedor.getRuc())) {
            throw new IllegalArgumentException("El RUC del proveedor es obligatorio.");
        }

        // Validar formato de RUC (11 dígitos)
        if (!proveedor.getRuc().matches("^\\d{11}$")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        // Validar que RUC empiece con 10, 15, 16, 17 o 20
        String primerDigito = proveedor.getRuc().substring(0, 2);
        if (!primerDigito.equals("10") && !primerDigito.equals("15") && 
            !primerDigito.equals("16") && !primerDigito.equals("17") && 
            !primerDigito.equals("20")) {
            throw new IllegalArgumentException("El RUC debe empezar con 10, 15, 16, 17 o 20.");
        }

        // Validar duplicado de RUC
        Optional<Proveedor> existenteRuc = proveedorRepository.findByRuc(proveedor.getRuc());
        if (existenteRuc.isPresent() && 
            (proveedor.getIdProveedor() == null || 
             !existenteRuc.get().getIdProveedor().equals(proveedor.getIdProveedor()))) {
            throw new DuplicateResourceException("El RUC " + proveedor.getRuc() + " ya está registrado para otro proveedor.");
        }

        // Validar razón social obligatoria
        if (!StringUtils.hasText(proveedor.getRazonSocial())) {
            throw new IllegalArgumentException("La razón social es obligatoria.");
        }

        // Validar email si está presente
        if (StringUtils.hasText(proveedor.getEmail())) {
            // Validar formato de email
            if (!proveedor.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("El formato del email no es válido.");
            }

            // Validar duplicado de email
            Optional<Proveedor> existenteEmail = proveedorRepository.findByEmail(proveedor.getEmail());
            if (existenteEmail.isPresent() && 
                (proveedor.getIdProveedor() == null || 
                 !existenteEmail.get().getIdProveedor().equals(proveedor.getIdProveedor()))) {
                throw new DuplicateResourceException("El email " + proveedor.getEmail() + " ya está registrado para otro proveedor.");
            }
        }
    }
}
