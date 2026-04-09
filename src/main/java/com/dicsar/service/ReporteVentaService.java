package com.dicsar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dicsar.dto.ClienteConMasComprasDTO;
import com.dicsar.dto.DashboardMetricasDTO;
import com.dicsar.dto.ProductoMasVendidoDTO;
import com.dicsar.dto.ReporteVentaDTO;
import com.dicsar.entity.Movimiento;
import com.dicsar.entity.Producto;
import com.dicsar.entity.ReporteVenta;
import com.dicsar.enums.TipoMovimiento;
import com.dicsar.exceptions.ResourceNotFoundException;
import com.dicsar.repository.MovimientoRepository;
import com.dicsar.repository.ProductoRepository;
import com.dicsar.repository.ReporteVentaRepository;
import com.dicsar.dto.PaginatedResponse;

@Service
public class ReporteVentaService {

    private final ReporteVentaRepository reporteVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    public ReporteVentaService(ReporteVentaRepository reporteVentaRepository) {
        this.reporteVentaRepository = reporteVentaRepository;
    }

    // ✅ NUEVO: Crear una venta (TRANSACCIONAL)
    @Transactional
    public ReporteVenta crear(ReporteVenta venta) {
        // 1. Validar que el producto existe
        Producto producto = productoRepository.findById(venta.getProducto().getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // 2. Validar que hay stock suficiente
        if (venta.getCantidad() > producto.getStockActual()) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStockActual());
        }

        // 3. Calcular y asignar precioUnitario y total
        Double precioUnitario = producto.getPrecio() != null ? producto.getPrecio() : 0.0;
        venta.setPrecioUnitario(precioUnitario);
        venta.setTotal(precioUnitario * venta.getCantidad());

        // 4. Crear el registro en reporte_venta
        ReporteVenta nuevaVenta = reporteVentaRepository.save(venta);

        // 4. Actualizar el stock del producto (SALIDA)
        producto.setStockActual(producto.getStockActual() - venta.getCantidad());
        productoRepository.save(producto);

        // 5. Registrar el movimiento (SALIDA de inventario)
        Movimiento movimiento = new Movimiento();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setCantidad(venta.getCantidad());
        movimiento.setDescripcion("Venta a cliente " + venta.getCliente().getNombre()
                + " - " + venta.getTipoDocumento() + " #" + nuevaVenta.getIdVenta());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        return nuevaVenta;
    }

    public List<ReporteVenta> listar() {
        return reporteVentaRepository.findAll();
    }

    public List<ReporteVentaDTO> listarDTO() {
        return reporteVentaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<ReporteVentaDTO> listarPaginado(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findAll(pageable);
        return convertPageToResponse(page);
    }

    public List<ReporteVenta> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return reporteVentaRepository.findByFechaVentaBetween(inicio, fin);
    }

    public PaginatedResponse<ReporteVentaDTO> obtenerPorRangoFechasPaginado(
            LocalDateTime inicio, LocalDateTime fin, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findByFechaVentaBetween(inicio, fin, pageable);
        return convertPageToResponse(page);
    }

    public List<ReporteVenta> obtenerVentasPorClientePaisa(Long idCliente) {
        return reporteVentaRepository.findByCliente(idCliente);
    }

    public PaginatedResponse<ReporteVentaDTO> obtenerVentasPorClientePaginado(
            Long idCliente, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findByCliente(idCliente, pageable);
        return convertPageToResponse(page);
    }

    public List<Object[]> obtenerProductosMasVendidos() {
        return reporteVentaRepository.obtenerProductosMasVendidos();
    }

    public List<Object[]> obtenerVentasPorCliente() {
        return reporteVentaRepository.obtenerVentasPorCliente();
    }

    public List<Object[]> obtenerTotalesMensuales() {
        return reporteVentaRepository.obtenerTotalesMensuales();
    }

    // Métodos para Dashboard y Reportes
    public DashboardMetricasDTO obtenerMetricasDashboard() {
        DashboardMetricasDTO metricas = new DashboardMetricasDTO();
        metricas.setTotalVentas(reporteVentaRepository.contarVentasCompletadas());
        metricas.setMontoTotalVentas(reporteVentaRepository.sumarMontoVentasCompletadas() != null
                ? reporteVentaRepository.sumarMontoVentasCompletadas()
                : 0.0);
        metricas.setVentasHoy(reporteVentaRepository.sumarMontoVentasHoy() != null
                ? reporteVentaRepository.sumarMontoVentasHoy()
                : 0.0);
        metricas.setVentasEstaSemana(reporteVentaRepository.sumarMontoVentasEstaSemana() != null
                ? reporteVentaRepository.sumarMontoVentasEstaSemana()
                : 0.0);
        metricas.setVentasEsteMes(reporteVentaRepository.sumarMontoVentasEsteMes() != null
                ? reporteVentaRepository.sumarMontoVentasEsteMes()
                : 0.0);
        return metricas;
    }

    public List<ClienteConMasComprasDTO> obtenerTop10ClientesPorCompras() {
        return reporteVentaRepository.obtenerTop10ClientesPorCompras();
    }

    public List<ProductoMasVendidoDTO> obtenerTop10ProductosMasVendidos() {
        return reporteVentaRepository.obtenerTop10ProductosMasVendidos();
    }

    private ReporteVentaDTO convertToDTO(ReporteVenta venta) {
        return ReporteVentaDTO.builder()
                .idVenta(venta.getIdVenta())
                .idCliente(venta.getCliente().getIdCliente())
                .nombreCliente(venta.getCliente().getNombre())
                .emailCliente(venta.getCliente().getEmail())
                .idProducto(venta.getProducto().getIdProducto())
                .nombreProducto(venta.getProducto().getNombre())
                .cantidad(venta.getCantidad())
                .precioUnitario(venta.getPrecioUnitario())
                .total(venta.getTotal())
                .tipoDocumento(venta.getTipoDocumento())
                .fechaVenta(venta.getFechaVenta())
                .estado(venta.getEstado())
                .build();
    }

    private PaginatedResponse<ReporteVentaDTO> convertPageToResponse(Page<ReporteVenta> page) {
        return PaginatedResponse.<ReporteVentaDTO>builder()
                .content(page.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }
}
