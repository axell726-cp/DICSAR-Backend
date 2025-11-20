package com.dicsar.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.dicsar.service.ExportService;

@RestController
@RequestMapping("/api/exportar")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/clientes/csv")
    public ResponseEntity<byte[]> exportarClientesCSV() {
        try {
            byte[] csvData = exportService.exportarClientesACSV();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            headers.set("Content-Disposition", "attachment; filename=clientes_" + System.currentTimeMillis() + ".csv");
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/clientes/excel")
    public ResponseEntity<byte[]> exportarClientesExcel() {
        try {
            byte[] excelData = exportService.exportarClientesAExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set("Content-Disposition", "attachment; filename=clientes_" + System.currentTimeMillis() + ".xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/clientes/pdf")
    public ResponseEntity<byte[]> exportarClientesPDF() {
        try {
            byte[] pdfData = exportService.exportarClientesAPDF();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set("Content-Disposition", "attachment; filename=clientes_" + System.currentTimeMillis() + ".pdf");
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
