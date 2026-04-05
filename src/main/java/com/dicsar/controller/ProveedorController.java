package com.dicsar.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.dicsar.dto.ProductoResponseDTO;
import com.dicsar.entity.Producto;
import com.dicsar.entity.Proveedor;
import com.dicsar.repository.ProductoRepository;
import com.dicsar.service.ProveedorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final ProductoRepository productoRepository;

    public ProveedorController(ProveedorService proveedorService, ProductoRepository productoRepository) {
        this.proveedorService = proveedorService;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return proveedorService.listar();
    }

    @GetMapping("/paginated")
    public Page<Proveedor> listarPaginado(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "razonSocial") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return proveedorService.listarPaginado(buscar, estado, pageable);
    }

    @GetMapping("/activos")
    public Page<Proveedor> listarActivos(
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("razonSocial").ascending());
        return proveedorService.buscarProveedoresActivos(buscar, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtener(@PathVariable Long id) {
        return proveedorService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/productos")
    public Page<ProductoResponseDTO> obtenerProductosPorProveedor(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> productosPage = productoRepository.buscarProductosPorProveedor(id, nombre, pageable);
        
        return productosPage.map(this::convertirAResponseDTO);
    }
    
    private ProductoResponseDTO convertirAResponseDTO(Producto p) {
        return ProductoResponseDTO.builder()
            .idProducto(p.getIdProducto())
            .nombre(p.getNombre())
            .codigo(p.getCodigo())
            .descripcion(p.getDescripcion())
            .precioBase(p.getPrecio())
            .stockActual(p.getStockActual())
            .stockMinimo(p.getStockMinimo())
            .estado(p.getEstado())
            .fechaVencimiento(p.getFechaVencimiento())
            .estadoVencimiento(p.getEstadoVencimiento())
            .fechaCreacion(p.getFechaCreacion())
            .fechaActualizacion(p.getFechaActualizacion())
            .categoriaId(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null)
            .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
            .unidadMedidaId(p.getUnidadMedida() != null ? p.getUnidadMedida().getIdUnidadMed() : null)
            .unidadMedidaNombre(p.getUnidadMedida() != null ? p.getUnidadMedida().getNombre() : null)
            .unidadMedidaAbreviatura(p.getUnidadMedida() != null ? p.getUnidadMedida().getAbreviatura() : null)
            .proveedorId(p.getProveedor() != null ? p.getProveedor().getIdProveedor() : null)
            .proveedorNombre(p.getProveedor() != null ? p.getProveedor().getRazonSocial() : null)
            .build();
    }

    @PostMapping
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody Proveedor proveedor) {
        Proveedor nuevo = proveedorService.guardar(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Proveedor proveedor) {
        Optional<Proveedor> existenteOpt = proveedorService.obtener(id);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proveedor no encontrado");
        }

        Proveedor existente = existenteOpt.get();
        existente.setRazonSocial(proveedor.getRazonSocial());
        existente.setRuc(proveedor.getRuc());
        existente.setDireccion(proveedor.getDireccion());
        existente.setTelefono(proveedor.getTelefono());
        existente.setEmail(proveedor.getEmail());
        existente.setContacto(proveedor.getContacto());
        existente.setFechaActualizacion(LocalDateTime.now());

        Proveedor actualizado = proveedorService.guardar(existente);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean nuevoEstado) {
        Optional<Proveedor> proveedorOpt = proveedorService.obtener(id);
        if (proveedorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proveedor no encontrado");
        }

        Proveedor proveedor = proveedorOpt.get();
        proveedor.setEstado(nuevoEstado);
        proveedor.setFechaActualizacion(LocalDateTime.now());
        proveedorService.guardar(proveedor);

        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.ok("Proveedor eliminado correctamente");
    }
}
