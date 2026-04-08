package com.dicsar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dicsar.dto.ClienteConMasComprasDTO;
import com.dicsar.dto.ProductoMasVendidoDTO;
import com.dicsar.entity.ReporteVenta;
import com.dicsar.entity.Cliente;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteVentaRepository extends JpaRepository<ReporteVenta, Long> {

       // Buscar ventas por rango de fechas
       @Query("SELECT r FROM ReporteVenta r WHERE r.fechaVenta BETWEEN :inicio AND :fin")
       List<ReporteVenta> findByFechaVentaBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT r FROM ReporteVenta r WHERE r.fechaVenta BETWEEN :inicio AND :fin")
       Page<ReporteVenta> findByFechaVentaBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin, Pageable pageable);

       // Obtener todas las ventas por cliente
       @Query("SELECT r FROM ReporteVenta r WHERE r.cliente.idCliente = :idCliente")
       List<ReporteVenta> findByCliente(@Param("idCliente") Long idCliente);

       @Query("SELECT r FROM ReporteVenta r WHERE r.cliente.idCliente = :idCliente")
       Page<ReporteVenta> findByCliente(@Param("idCliente") Long idCliente, Pageable pageable);

       // Productos más vendidos (con ID del producto y total en moneda)
       @Query("SELECT r.producto.idProducto, r.producto.nombre, SUM(r.cantidad) as totalCantidad, SUM(r.total) as totalVentas FROM ReporteVenta r WHERE r.estado = true GROUP BY r.producto.idProducto, r.producto.nombre ORDER BY SUM(r.cantidad) DESC")
       List<Object[]> obtenerProductosMasVendidos();

       // Total de ventas por cliente (con ID del cliente)
       @Query("SELECT r.cliente.idCliente, r.cliente.nombre, SUM(r.total) as totalVentas, COUNT(r) as cantidadCompras FROM ReporteVenta r WHERE r.estado = true GROUP BY r.cliente.idCliente, r.cliente.nombre ORDER BY SUM(r.total) DESC")
       List<Object[]> obtenerVentasPorCliente();

       // Totales mensuales (con año y mes formateado)
       @Query("SELECT YEAR(r.fechaVenta), MONTH(r.fechaVenta), SUM(r.total) as totalMensual, COUNT(r) as cantidadVentas FROM ReporteVenta r WHERE r.estado = true GROUP BY YEAR(r.fechaVenta), MONTH(r.fechaVenta) ORDER BY YEAR(r.fechaVenta) DESC, MONTH(r.fechaVenta) DESC")
       List<Object[]> obtenerTotalesMensuales();

       // Contar total de ventas
       @Query("SELECT COUNT(r) FROM ReporteVenta r WHERE r.estado = true")
       Long contarVentasCompletadas();

       // Sumar monto total de ventas
       @Query("SELECT SUM(r.total) FROM ReporteVenta r WHERE r.estado = true")
       Double sumarMontoVentasCompletadas();

       // Sumar ventas de hoy
       @Query("SELECT SUM(r.total) FROM ReporteVenta r WHERE r.estado = true AND DATE(r.fechaVenta) = CURRENT_DATE")
       Double sumarMontoVentasHoy();

       // Sumar ventas de esta semana
       @Query("SELECT SUM(r.total) FROM ReporteVenta r WHERE r.estado = true AND YEARWEEK(r.fechaVenta) = YEARWEEK(NOW())")
       Double sumarMontoVentasEstaSemana();

       // Sumar ventas de este mes
       @Query("SELECT SUM(r.total) FROM ReporteVenta r WHERE r.estado = true AND YEAR(r.fechaVenta) = YEAR(NOW()) AND MONTH(r.fechaVenta) = MONTH(NOW())")
       Double sumarMontoVentasEsteMes();

       // Top 10 clientes por cantidad de compras
       @Query("SELECT new com.dicsar.dto.ClienteConMasComprasDTO(r.cliente.idCliente, r.cliente.nombre, COUNT(r), SUM(r.total)) "
                     +
                     "FROM ReporteVenta r WHERE r.estado = true " +
                     "GROUP BY r.cliente.idCliente, r.cliente.nombre " +
                     "ORDER BY COUNT(r) DESC")
       List<ClienteConMasComprasDTO> obtenerTop10ClientesPorCompras();

       // Top 10 productos más vendidos
       @Query("SELECT new com.dicsar.dto.ProductoMasVendidoDTO(r.producto.idProducto, r.producto.nombre, SUM(r.cantidad), SUM(r.total)) "
                     +
                     "FROM ReporteVenta r WHERE r.estado = true " +
                     "GROUP BY r.producto.idProducto, r.producto.nombre " +
                     "ORDER BY SUM(r.cantidad) DESC")
       List<ProductoMasVendidoDTO> obtenerTop10ProductosMasVendidos();
}
