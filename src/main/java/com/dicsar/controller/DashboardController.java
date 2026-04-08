package com.dicsar.controller;

import com.dicsar.dto.DashboardMetricasDTO;
import com.dicsar.service.ClienteService;
import com.dicsar.service.ProductoService;
import com.dicsar.service.ReporteVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
public class DashboardController {

    @Autowired
    private ReporteVentaService reporteVentaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @GetMapping("/metricas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<DashboardMetricasDTO> obtenerMetricas() {
        DashboardMetricasDTO metricas = reporteVentaService.obtenerMetricasDashboard();

        // Completar campos adicionales desde otros servicios
        metricas.setTotalClientes(clienteService.countClientesActivos());
        metricas.setTotalProductos(productoService.countProductos());
        metricas.setProductosAgotados(productoService.countProductosAgotados());

        return ResponseEntity.ok(metricas);
    }
}
