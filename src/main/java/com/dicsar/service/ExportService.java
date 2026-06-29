package com.dicsar.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Div;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.HorizontalAlignment;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.dicsar.dto.ClienteDTO;
import com.dicsar.dto.ReporteVentaDTO;
import com.dicsar.entity.ReporteVenta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReporteVentaService reporteVentaService;

    @Autowired
    private com.dicsar.repository.ReporteVentaRepository reporteVentaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Definir colores del tema DICSAR (azul)
    private static final Color PRIMARY_BLUE = new DeviceRgb(59, 130, 246);
    private static final Color LIGHT_BLUE = new DeviceRgb(219, 234, 254);
    private static final Color DARK_BLUE = new DeviceRgb(30, 58, 138);
    private static final Color GRAY_TEXT = new DeviceRgb(100, 116, 139);

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
     * Exportar clientes a PDF con diseño bonito
     */
    public byte[] exportarClientesAPDF() throws IOException {
        List<ClienteDTO> clientes = clienteService.listarClientesActivos();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        // Encabezado del documento
        addDocumentHeader(document, "Reporte de Clientes");
        document.add(new Paragraph(" ").setMarginBottom(10));

        // Crear tabla
        Table table = new Table(11);
        table.useAllAvailableWidth();

        // Añadir encabezados
        String[] headers = { "ID", "Nombre", "Apellidos", "Tipo Doc.", "Número",
                "Razón Social", "Teléfono", "Email", "Dirección", "Tipo", "Estado" };

        for (String h : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(PRIMARY_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                    .setBold()
                    .setPadding(8)
                    .add(new Paragraph(h).setFontSize(10));
            table.addHeaderCell(headerCell);
        }

        // Añadir filas
        for (ClienteDTO cliente : clientes) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(cliente.getIdCliente())).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getNombre() != null ? cliente.getNombre() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getApellidos() != null ? cliente.getApellidos() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getTelefono() != null ? cliente.getTelefono() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getEmail() != null ? cliente.getEmail() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getDireccion() != null ? cliente.getDireccion() : "").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getEsEmpresa() ? "Empresa" : "Persona").setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cliente.getEstado() ? "Activo" : "Inactivo").setFontSize(9)));
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

    /**
     * Exportar ventas de un cliente a Excel
     */
    public byte[] exportarVentasAExcel(Long idCliente) throws IOException {
        List<ReporteVenta> ventas = reporteVentaService.obtenerVentasPorClientePaisa(idCliente);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ventas Cliente");

        String[] headers = { "ID VENTA", "FECHA", "PRODUCTO", "CANTIDAD", "PRECIO UNIT.", "SUBTOTAL", "IGV", "TOTAL", "TIPO DOC.", "ESTADO" };

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNumber = 1;
        for (ReporteVenta venta : ventas) {
            Row row = sheet.createRow(rowNumber++);
            row.createCell(0).setCellValue(venta.getIdVenta());
            row.createCell(1).setCellValue(venta.getFechaVenta().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(venta.getProducto().getNombre());
            row.createCell(3).setCellValue(venta.getCantidad());
            row.createCell(4).setCellValue(venta.getPrecioUnitario());
            row.createCell(5).setCellValue(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0);
            row.createCell(6).setCellValue(venta.getIgv() != null ? venta.getIgv() : 0.0);
            row.createCell(7).setCellValue(venta.getTotal());
            row.createCell(8).setCellValue(venta.getTipoDocumento());
            row.createCell(9).setCellValue(venta.getEstado() ? "Activa" : "Anulada");
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    /**
     * Exportar ventas de un cliente a PDF con diseño bonito
     */
    public byte[] exportarVentasAPDF(Long idCliente) throws IOException {
        List<ReporteVenta> ventas = reporteVentaService.obtenerVentasPorClientePaisa(idCliente);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        // Obtener nombre del cliente
        String clienteNombre = "";
        if (!ventas.isEmpty()) {
            clienteNombre = ventas.get(0).getCliente().getNombre() + " "
                    + (ventas.get(0).getCliente().getApellidos() != null ? ventas.get(0).getCliente().getApellidos() : "");
        }

        // Encabezado del documento
        addDocumentHeader(document, "Historial de Ventas - " + clienteNombre);

        // Información del cliente
        if (!ventas.isEmpty()) {
            Paragraph clienteInfo = new Paragraph("Cliente: " + clienteNombre)
                    .setFontSize(12)
                    .setFontColor(DARK_BLUE);
            document.add(clienteInfo.setMarginBottom(10));
        }

        document.add(new Paragraph(" ").setMarginBottom(10));

        Table table = new Table(10);
        table.useAllAvailableWidth();

        String[] headers = { "ID VENTA", "FECHA", "PRODUCTO", "CANTIDAD", "PRECIO UNIT.", "SUBTOTAL", "IGV", "TOTAL", "TIPO DOC.", "ESTADO" };
        for (String h : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(PRIMARY_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                    .setBold()
                    .setPadding(6)
                    .add(new Paragraph(h).setFontSize(9));
            table.addHeaderCell(headerCell);
        }

        for (ReporteVenta venta : ventas) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getIdVenta())).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(venta.getFechaVenta().format(DATE_FORMATTER)).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(venta.getProducto().getNombre()).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getCantidad()))).setTextAlignment(TextAlignment.CENTER).setFontSize(9));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getPrecioUnitario())).setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0))).setFontSize(9));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getIgv() != null ? venta.getIgv() : 0.0))).setFontSize(9));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getTotal()))).setFontSize(9));
            table.addCell(new Cell().add(new Paragraph(venta.getTipoDocumento() != null ? venta.getTipoDocumento() : "")).setFontSize(9));
            table.addCell(new Cell().add(new Paragraph(venta.getEstado() ? "Activa" : "Anulada").setFontSize(9)));
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Exportar comprobante/venta individual a PDF con diseño bonito
     */
    public byte[] exportarComprobantePDF(Long idVenta) throws IOException {
        java.util.Optional<ReporteVenta> opt = reporteVentaRepository.findById(idVenta);
        if (opt.isEmpty()) throw new IllegalArgumentException("Venta no encontrada");
        ReporteVenta venta = opt.get();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        // Encabezado
        addDocumentHeader(document, "Comprobante de Venta");

        // Tabla de información principal
        Table infoTable = new Table(2);
        infoTable.useAllAvailableWidth();

        String clienteCompleto = venta.getCliente().getNombre() + " "
                + (venta.getCliente().getApellidos() != null ? venta.getCliente().getApellidos() : "");

        // Añadir filas
        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Número comprobante")));
        infoTable.addCell(new Cell().add(new Paragraph(venta.getComprobanteNumero() != null ? String.valueOf(venta.getComprobanteNumero()) : "-").setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Cliente")));
        infoTable.addCell(new Cell().add(new Paragraph(clienteCompleto).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Fecha")));
        infoTable.addCell(new Cell().add(new Paragraph(venta.getFechaVenta().format(DATE_FORMATTER)).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Producto")));
        infoTable.addCell(new Cell().add(new Paragraph(venta.getProducto().getNombre()).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Cantidad")));
        infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getCantidad())).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Precio Unitario")));
        infoTable.addCell(new Cell().add(new Paragraph("S/ " + String.valueOf(venta.getPrecioUnitario())).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("Subtotal")));
        infoTable.addCell(new Cell().add(new Paragraph("S/ " + String.valueOf(venta.getSubtotal())).setFontSize(10)));

        infoTable.addCell(new Cell().setBackgroundColor(LIGHT_BLUE)
                .setFontColor(DARK_BLUE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("IGV (18%)")));
        infoTable.addCell(new Cell().add(new Paragraph("S/ " + String.valueOf(venta.getIgv())).setFontSize(10)));

        // Total en negrita
        infoTable.addCell(new Cell().setBackgroundColor(PRIMARY_BLUE)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setBold()
                .setPadding(6)
                .add(new Paragraph("TOTAL")));
        infoTable.addCell(new Cell().setBold()
                .add(new Paragraph("S/ " + String.valueOf(venta.getTotal())).setFontSize(12)));

        document.add(infoTable.setMarginTop(10));
        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Método helper para añadir encabezado a los PDFs con marca DICSAR
     */
    private void addDocumentHeader(Document document, String title) {
        // Encabezado con DICSAR
        Paragraph brand = new Paragraph("DICSAR")
                .setFontSize(28)
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(brand);

        // Título del reporte
        Paragraph titlePara = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setFontColor(DARK_BLUE)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titlePara.setMarginBottom(5));

        // Fecha del reporte
        Paragraph datePara = new Paragraph("Fecha de generación: " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(datePara.setMarginBottom(15));

        // Línea separadora
        Div divider = new Div()
                .setHeight(2)
                .setBackgroundColor(PRIMARY_BLUE)
                .setMarginBottom(15);
        document.add(divider);
    }
}
