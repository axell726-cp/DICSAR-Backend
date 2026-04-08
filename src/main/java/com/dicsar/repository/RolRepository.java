package com.dicsar.repository;

import com.dicsar.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Integer> {

    boolean existsByNombre(String nombre);

    Optional<RolEntity> findByNombre(String nombre);

    List<RolEntity> findByActivo(Boolean activo);
}
