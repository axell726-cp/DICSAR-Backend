package com.dicsar.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dicsar.dto.ProveedorConMasProductosDTO;
import com.dicsar.dto.ReporteInventarioDTO;
import com.dicsar.dto.ReporteProveedoresDTO;
import com.dicsar.entity.Producto;
import com.dicsar.entity.Proveedor;
import com.dicsar.repository.CategoriaRepository;
import com.dicsar.repository.ProductoRepository;
import com.dicsar.repository.ProveedorRepository;

@Service
public class ReporteService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;

    public ReporteService(ProductoRepository productoRepository, 
                         ProveedorRepository proveedorRepository,
                         CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public ReporteInventarioDTO generarReporteInventario() {
        List<Producto> productos = productoRepository.findAll();

        long totalProductos = productos.size();
        long productosActivos = productos.stream().filter(p -> Boolean.TRUE.equals(p.getEstado())).count();
        long productosInactivos = totalProductos - productosActivos;
        
        long productosConStockBajo = productos.stream()
            .filter(p -> p.getStockActual() != null && p.getStockMinimo() != null)
            .filter(p -> p.getStockActual() > 0 && p.getStockActual() <= p.getStockMinimo())
            .count();
        
        long productosSinStock = productos.stream()
            .filter(p -> p.getStockActual() == null || p.getStockActual() == 0)
            .count();

        double valorTotalInventario = productos.stream()
            .filter(p -> Boolean.TRUE.equals(p.getEstado()))
            .mapToDouble(p -> {
                double precio = p.getPrecio() != null ? p.getPrecio() : 0.0;
                int stock = p.getStockActual() != null ? p.getStockActual() : 0;
                return precio * stock;
            })
            .sum();

        long totalCategorias = categoriaRepository.count();

        int stockTotalActual = productos.stream()
            .filter(p -> Boolean.TRUE.equals(p.getEstado()))
            .mapToInt(p -> p.getStockActual() != null ? p.getStockActual() : 0)
            .sum();

        return ReporteInventarioDTO.builder()
            .totalProductos(totalProductos)
            .productosActivos(productosActivos)
            .productosInactivos(productosInactivos)
            .productosConStockBajo(productosConStockBajo)
            .productosSinStock(productosSinStock)
            .valorTotalInventario(valorTotalInventario)
            .totalCategorias(totalCategorias)
            .stockTotalActual(stockTotalActual)
            .build();
    }

    public ReporteProveedoresDTO generarReporteProveedores() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Producto> productos = productoRepository.findAll();

        long totalProveedores = proveedores.size();
        long proveedoresActivos = proveedores.stream()
            .filter(p -> Boolean.TRUE.equals(p.getEstado()))
            .count();
        long proveedoresInactivos = totalProveedores - proveedoresActivos;

        long totalProductosPorProveedores = productos.stream()
            .filter(p -> p.getProveedor() != null)
            .count();

        // Contar productos por proveedor
        Map<Long, Long> productosPorProveedor = productos.stream()
            .filter(p -> p.getProveedor() != null && p.getProveedor().getIdProveedor() != null)
            .collect(Collectors.groupingBy(
                p -> p.getProveedor().getIdProveedor(),
                Collectors.counting()
            ));

        double promedioProductosPorProveedor = productosPorProveedor.isEmpty() ? 0.0 :
            productosPorProveedor.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        // Encontrar proveedor con más productos
        ProveedorConMasProductosDTO proveedorConMasProductos = null;
        if (!productosPorProveedor.isEmpty()) {
            Map.Entry<Long, Long> maxEntry = productosPorProveedor.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);

            if (maxEntry != null) {
                Proveedor prov = proveedorRepository.findById(maxEntry.getKey()).orElse(null);
                if (prov != null) {
                    proveedorConMasProductos = ProveedorConMasProductosDTO.builder()
                        .idProveedor(prov.getIdProveedor())
                        .razonSocial(prov.getRazonSocial())
                        .ruc(prov.getRuc())
                        .cantidadProductos(maxEntry.getValue())
                        .build();
                }
            }
        }

        return ReporteProveedoresDTO.builder()
            .totalProveedores(totalProveedores)
            .proveedoresActivos(proveedoresActivos)
            .proveedoresInactivos(proveedoresInactivos)
            .totalProductosPorProveedores(totalProductosPorProveedores)
            .promedioProductosPorProveedor(promedioProductosPorProveedor)
            .proveedorConMasProductos(proveedorConMasProductos)
            .build();
    }
}
