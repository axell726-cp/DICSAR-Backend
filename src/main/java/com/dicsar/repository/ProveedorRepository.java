package com.dicsar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dicsar.entity.Proveedor;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByRuc(String ruc);
    
    Optional<Proveedor> findByEmail(String email);

    boolean existsByRuc(String ruc);

    boolean existsByEmail(String email);

    // Búsqueda por nombre o RUC con paginación
    @Query("SELECT p FROM Proveedor p WHERE " +
           "(:buscar IS NULL OR :buscar = '' OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "p.ruc LIKE CONCAT('%', :buscar, '%'))")
    Page<Proveedor> buscarProveedores(@Param("buscar") String buscar, Pageable pageable);

    // Buscar solo activos
    @Query("SELECT p FROM Proveedor p WHERE p.estado = true AND " +
           "(:buscar IS NULL OR :buscar = '' OR " +
           "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :buscar, '%')) OR " +
           "p.ruc LIKE CONCAT('%', :buscar, '%'))")
    Page<Proveedor> buscarProveedoresActivos(@Param("buscar") String buscar, Pageable pageable);

    // Buscar por estado
    Page<Proveedor> findByEstado(Boolean estado, Pageable pageable);
}
