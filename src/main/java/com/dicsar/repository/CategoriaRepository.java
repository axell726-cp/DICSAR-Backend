package com.dicsar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dicsar.entity.Categoria;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
