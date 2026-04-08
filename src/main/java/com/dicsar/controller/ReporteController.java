package com.dicsar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dicsar.dto.ClienteConMasComprasDTO;
import com.dicsar.dto.ReporteInventarioDTO;
import com.dicsar.dto.ReporteProveedoresDTO;
import com.dicsar.service.ReporteService;
import com.dicsar.service.ReporteVentaService;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
public class ReporteController {

    private final ReporteService reporteService;
    private final ReporteVentaService reporteVentaService;

    public ReporteController(ReporteService reporteService, ReporteVentaService reporteVentaService) {
        this.reporteService = reporteService;
        this.reporteVentaService = reporteVentaService;
    }

    @GetMapping("/inventario")
    public ResponseEntity<ReporteInventarioDTO> obtenerReporteInventario() {
        ReporteInventarioDTO reporte = reporteService.generarReporteInventario();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/proveedores")
    public ResponseEntity<ReporteProveedoresDTO> obtenerReporteProveedores() {
        ReporteProveedoresDTO reporte = reporteService.generarReporteProveedores();
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ventas-por-cliente")
    public ResponseEntity<List<ClienteConMasComprasDTO>> obtenerVentasPorCliente() {
        List<ClienteConMasComprasDTO> ventasPorCliente = reporteVentaService.obtenerTop10ClientesPorCompras();
        return ResponseEntity.ok(ventasPorCliente);
    }
}
