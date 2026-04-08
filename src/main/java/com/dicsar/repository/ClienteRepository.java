package com.dicsar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import com.dicsar.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    // Búsqueda por nombre o apellido
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Cliente> buscarPorNombre(@Param("search") String search, Pageable pageable);

    // Búsqueda por email
    Page<Cliente> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Búsqueda por teléfono
    Page<Cliente> findByTelefonoContainingIgnoreCase(String telefono, Pageable pageable);

    // Buscar por tipo de cliente
    Page<Cliente> findByEsEmpresa(Boolean esEmpresa, Pageable pageable);

    // Buscar activos
    Page<Cliente> findByEstado(Boolean estado, Pageable pageable);

    // Obtener todos con paginación
    Page<Cliente> findAll(Pageable pageable);

    // Listar clientes activos
    List<Cliente> findByEstadoTrue();

    // Contar clientes activos
    Long countByEstadoTrue();
}
