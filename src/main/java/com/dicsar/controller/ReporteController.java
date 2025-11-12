package com.dicsar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dicsar.dto.ReporteInventarioDTO;
import com.dicsar.dto.ReporteProveedoresDTO;
import com.dicsar.service.ReporteService;

@RestController
@RequestMapping("api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
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
}
