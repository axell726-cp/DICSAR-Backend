package com.dicsar.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.dicsar.dto.ClienteDTO;
import com.dicsar.dto.ReporteVentaDTO;
import com.dicsar.entity.ReporteVenta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReporteVentaService reporteVentaService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exportar clientes a CSV
     */
    public byte[] exportarClientesACSV() throws IOException {
        List<ClienteDTO> clientes = clienteService.listarClientesActivos();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        CSVFormat format = CSVFormat.DEFAULT.withHeader(
                "ID", "NOMBRE", "APELLIDOS", "TIPO DOCUMENTO", "NÚMERO DOCUMENTO",
                "RAZÓN SOCIAL", "TELÉFONO", "EMAIL", "DIRECCIÓN", "TIPO CLIENTE", "ESTADO");

        CSVPrinter printer = new CSVPrinter(writer, format);

        for (ClienteDTO cliente : clientes) {
            printer.printRecord(
                    cliente.getIdCliente(),
                    cliente.getNombre(),
                    cliente.getApellidos(),
                    cliente.getTipoDocumento(),
                    cliente.getNumeroDocumento(),
                    cliente.getRazonSocial(),
                    cliente.getTelefono(),
                    cliente.getEmail(),
                    cliente.getDireccion(),
                    cliente.getEsEmpresa() ? "Empresa" : "Persona",
                    cliente.getEstado() ? "Activo" : "Inactivo");
        }

        printer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Exportar clientes a Excel
     */
    public byte[] exportarClientesAExcel() throws IOException {
        List<ClienteDTO> clientes = clienteService.listarClientesActivos();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Clientes");

        // Crear encabezado
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "NOMBRE", "APELLIDOS", "TIPO DOCUMENTO", "NÚMERO DOCUMENTO",
                "RAZÓN SOCIAL", "TELÉFONO", "EMAIL", "DIRECCIÓN", "TIPO CLIENTE", "ESTADO" };

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos
        int rowNumber = 1;
        for (ClienteDTO cliente : clientes) {
            Row row = sheet.createRow(rowNumber++);
            row.createCell(0).setCellValue(cliente.getIdCliente());
            row.createCell(1).setCellValue(cliente.getNombre());
            row.createCell(2).setCellValue(cliente.getApellidos());
            row.createCell(3).setCellValue(cliente.getTipoDocumento());
            row.createCell(4).setCellValue(cliente.getNumeroDocumento());
            row.createCell(5).setCellValue(cliente.getRazonSocial());
            row.createCell(6).setCellValue(cliente.getTelefono());
            row.createCell(7).setCellValue(cliente.getEmail());
            row.createCell(8).setCellValue(cliente.getDireccion());
            row.createCell(9).setCellValue(cliente.getEsEmpresa() ? "Empresa" : "Persona");
            row.createCell(10).setCellValue(cliente.getEstado() ? "Activo" : "Inactivo");
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Exportar clientes a PDF
     */
    public byte[] exportarClientesAPDF() throws IOException {
        List<ClienteDTO> clientes = clienteService.listarClientesActivos();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Reporte de Clientes").setFontSize(20).setBold());
        document.add(new Paragraph(" "));

        // Crear tabla
        Table table = new Table(11);
        table.addHeaderCell(new Cell().add(new Paragraph("ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre")));
        table.addHeaderCell(new Cell().add(new Paragraph("Apellidos")));
        table.addHeaderCell(new Cell().add(new Paragraph("Tipo Doc.")));
        table.addHeaderCell(new Cell().add(new Paragraph("Número")));
        table.addHeaderCell(new Cell().add(new Paragraph("Razón Social")));
        table.addHeaderCell(new Cell().add(new Paragraph("Teléfono")));
        table.addHeaderCell(new Cell().add(new Paragraph("Email")));
        table.addHeaderCell(new Cell().add(new Paragraph("Dirección")));
        table.addHeaderCell(new Cell().add(new Paragraph("Tipo")));
        table.addHeaderCell(new Cell().add(new Paragraph("Estado")));

        for (ClienteDTO cliente : clientes) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(cliente.getIdCliente()))));
            table.addCell(new Cell().add(new Paragraph(cliente.getNombre() != null ? cliente.getNombre() : "")));
            table.addCell(new Cell().add(new Paragraph(cliente.getApellidos() != null ? cliente.getApellidos() : "")));
            table.addCell(new Cell()
                    .add(new Paragraph(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento() : "")));
            table.addCell(new Cell()
                    .add(new Paragraph(cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "")));
            table.addCell(
                    new Cell().add(new Paragraph(cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "")));
            table.addCell(new Cell().add(new Paragraph(cliente.getTelefono() != null ? cliente.getTelefono() : "")));
            table.addCell(new Cell().add(new Paragraph(cliente.getEmail() != null ? cliente.getEmail() : "")));
            table.addCell(new Cell().add(new Paragraph(cliente.getDireccion() != null ? cliente.getDireccion() : "")));
            table.addCell(new Cell().add(new Paragraph(cliente.getEsEmpresa() ? "Empresa" : "Persona")));
            table.addCell(new Cell().add(new Paragraph(cliente.getEstado() ? "Activo" : "Inactivo")));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    /**
     * Exportar ventas a CSV
     */
    public byte[] exportarVentasACSV(Long idCliente) throws IOException {
        List<ReporteVenta> ventas = reporteVentaService.obtenerVentasPorClientePaisa(idCliente);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        CSVFormat format = CSVFormat.DEFAULT.withHeader(
                "ID VENTA", "CLIENTE", "PRODUCTO", "CANTIDAD", "PRECIO UNITARIO",
                "TOTAL", "TIPO DOCUMENTO", "FECHA VENTA", "ESTADO");

        CSVPrinter printer = new CSVPrinter(writer, format);

        for (ReporteVenta venta : ventas) {
            printer.printRecord(
                    venta.getIdVenta(),
                    venta.getCliente().getNombre(),
                    venta.getProducto().getNombre(),
                    venta.getCantidad(),
                    venta.getPrecioUnitario(),
                    venta.getTotal(),
                    venta.getTipoDocumento(),
                    venta.getFechaVenta().format(DATE_FORMATTER),
                    venta.getEstado() ? "Activa" : "Anulada");
        }

        printer.flush();
        writer.close();

        return outputStream.toByteArray();
    }

    /**
     * Exportar todas las ventas a CSV
     */
    public byte[] exportarTodasVentasACSV() throws IOException {
        List<ReporteVenta> ventas = reporteVentaService.listar();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        CSVFormat format = CSVFormat.DEFAULT.withHeader(
                "ID VENTA", "CLIENTE", "PRODUCTO", "CANTIDAD", "PRECIO UNITARIO",
                "TOTAL", "TIPO DOCUMENTO", "FECHA VENTA", "ESTADO");

        CSVPrinter printer = new CSVPrinter(writer, format);

        for (ReporteVenta venta : ventas) {
            printer.printRecord(
                    venta.getIdVenta(),
                    venta.getCliente().getNombre(),
                    venta.getProducto().getNombre(),
                    venta.getCantidad(),
                    venta.getPrecioUnitario(),
                    venta.getTotal(),
                    venta.getTipoDocumento(),
                    venta.getFechaVenta().format(DATE_FORMATTER),
                    venta.getEstado() ? "Activa" : "Anulada");
        }

        printer.flush();
        writer.close();

        return outputStream.toByteArray();
    }
}
