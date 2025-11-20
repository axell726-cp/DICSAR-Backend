package com.dicsar.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

import com.dicsar.dto.PaginatedResponse;
import com.dicsar.dto.ReporteVentaDTO;
import com.dicsar.entity.ReporteVenta;
import com.dicsar.service.ExportService;
import com.dicsar.service.ReporteVentaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reportes-ventas")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@Validated
public class ReporteVentaController {

    private final ReporteVentaService reporteVentaService;
    private final ExportService exportService;

    public ReporteVentaController(ReporteVentaService reporteVentaService, ExportService exportService) {
        this.reporteVentaService = reporteVentaService;
        this.exportService = exportService;
    }

    // Registrar una nueva venta
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ReporteVenta> crearVenta(@Valid @RequestBody ReporteVenta venta) {
        ReporteVenta nuevaVenta = reporteVentaService.crear(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ReporteVenta>> listar() {
        return ResponseEntity.ok(reporteVentaService.listar());
    }

    @GetMapping("/listar-dto")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ReporteVentaDTO>> listarDTO() {
        return ResponseEntity.ok(reporteVentaService.listarDTO());
    }

    @GetMapping("/pagina")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaginatedResponse<ReporteVentaDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ReporteVentaDTO> response = reporteVentaService.listarPaginado(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rango-fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ReporteVenta>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(reporteVentaService.obtenerPorRangoFechas(inicio, fin));
    }

    @GetMapping("/rango-fechas/pagina")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaginatedResponse<ReporteVentaDTO>> obtenerPorRangoFechasPaginado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ReporteVentaDTO> response = reporteVentaService.obtenerPorRangoFechasPaginado(
                inicio, fin, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<PaginatedResponse<ReporteVentaDTO>> obtenerVentasPorCliente(
            @PathVariable Long idCliente,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginatedResponse<ReporteVentaDTO> response = reporteVentaService.obtenerVentasPorClientePaginado(
                idCliente, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/productos-mas-vendidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Object[]>> obtenerProductosMasVendidos() {
        return ResponseEntity.ok(reporteVentaService.obtenerProductosMasVendidos());
    }

    @GetMapping("/ventas-por-cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Object[]>> obtenerVentasPorCliente() {
        return ResponseEntity.ok(reporteVentaService.obtenerVentasPorCliente());
    }

    @GetMapping("/totales-mensuales")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Object[]>> obtenerTotalesMensuales() {
        return ResponseEntity.ok(reporteVentaService.obtenerTotalesMensuales());
    }

    // Exportación a CSV
    @GetMapping("/exportar/todas/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<byte[]> exportarTodasVentasCSV() {
        try {
            byte[] csvData = exportService.exportarTodasVentasACSV();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            headers.set("Content-Disposition", "attachment; filename=ventas_" + System.currentTimeMillis() + ".csv");
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exportar/cliente/{idCliente}/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<byte[]> exportarVentasClienteCSV(@PathVariable Long idCliente) {
        try {
            byte[] csvData = exportService.exportarVentasACSV(idCliente);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            headers.set("Content-Disposition",
                    "attachment; filename=ventas_cliente_" + idCliente + "_" + System.currentTimeMillis() + ".csv");
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
