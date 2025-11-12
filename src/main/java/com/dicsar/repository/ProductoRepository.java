package com.dicsar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dicsar.entity.Producto;
import com.dicsar.enums.EstadoVencimiento;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Buscar producto por código
    Optional<Producto> findByCodigo(String codigo);
    
    // Validar existencia de producto por código
    boolean existsByCodigo(String codigo);
    
    boolean existsByNombreAndCategoriaIdCategoria(String nombre, Long idCategoria);
    
    Optional<Producto> findByNombreAndCategoriaIdCategoria(String nombre, Long idCategoria);

    @Query("""
    	    SELECT p FROM Producto p
    	    WHERE (:categoriaId IS NULL OR p.categoria.idCategoria = :categoriaId)
    	      AND (:proveedorId IS NULL OR p.proveedor.idProveedor = :proveedorId)
    	      AND (:estadoVencimiento IS NULL OR p.estadoVencimiento = :estadoVencimiento)
    	      AND (:stockMin IS NULL OR p.stockActual >= :stockMin)
    	      AND (:stockMax IS NULL OR p.stockActual <= :stockMax)
    	""")
    	List<Producto> filtrarStock(
    	        @Param("categoriaId") Long categoriaId,
    	        @Param("proveedorId") Long proveedorId,
    	        @Param("estadoVencimiento") EstadoVencimiento estadoVencimiento,
    	        @Param("stockMin") Integer stockMin,
    	        @Param("stockMax") Integer stockMax
    	);

    // Encontrar productos por proveedor con paginación
    Page<Producto> findByProveedorIdProveedor(Long idProveedor, Pageable pageable);

    // Encontrar productos por proveedor
    List<Producto> findByProveedorIdProveedor(Long idProveedor);

    // Encontrar productos por categoría
    List<Producto> findByCategoriaIdCategoria(Long idCategoria);

    // Contar productos por proveedor
    long countByProveedorIdProveedor(Long idProveedor);

    // Buscar productos por proveedor con filtro de nombre
    @Query("SELECT p FROM Producto p WHERE p.proveedor.idProveedor = :proveedorId " +
           "AND (:nombre IS NULL OR :nombre = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Producto> buscarProductosPorProveedor(@Param("proveedorId") Long proveedorId, 
                                                 @Param("nombre") String nombre, 
                                                 Pageable pageable);
}
