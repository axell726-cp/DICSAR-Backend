package com.dicsar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReporteVentaService {

    private final ReporteVentaRepository reporteVentaRepository;

    private static final Logger logger = LoggerFactory.getLogger(ReporteVentaService.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReporteVentaService(ReporteVentaRepository reporteVentaRepository) {
        this.reporteVentaRepository = reporteVentaRepository;
    }

    // ✅ NUEVO: Crear una venta (TRANSACCIONAL)
    @Transactional
    public ReporteVenta crear(ReporteVenta venta) {
        try {
            if (venta == null || venta.getProducto() == null || venta.getProducto().getIdProducto() == null) {
                throw new IllegalArgumentException("Debe indicar el producto de la venta.");
            }
            if (venta.getCantidad() == null || venta.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de la venta debe ser mayor a cero.");
            }

            // 1. Validar que el producto existe
            Producto producto = productoRepository.findByIdForUpdate(venta.getProducto().getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // 2. Validar que hay stock suficiente
            if (venta.getCantidad() > producto.getStockActual()) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStockActual());
            }

            // 3. Calcular y asignar precioUnitario
            Double precioUnitario = producto.getPrecio() != null ? producto.getPrecio() : 0.0;
            venta.setPrecioUnitario(precioUnitario);

            // Calcular subtotal, IGV y total
            calcularTotales(venta);

            // Asignar numeración secuencial de comprobante (dentro de la misma transacción)
            Long numero = nextComprobanteNumber();
            venta.setComprobanteNumero(numero);

            // 4. Crear el registro en reporte_venta
            logger.debug("Guardando reporte_venta: clienteId={}, productoId={}, cantidad={}, total={}",
                    venta != null && venta.getCliente() != null ? venta.getCliente().getIdCliente() : null,
                    venta != null && venta.getProducto() != null ? venta.getProducto().getIdProducto() : null,
                    venta != null ? venta.getCantidad() : null,
                    venta != null ? venta.getTotal() : null);

            ReporteVenta nuevaVenta;
            try {
                nuevaVenta = reporteVentaRepository.saveAndFlush(venta);
            } catch (Exception exSaveReporte) {
                logger.error("Fallo al guardar reporte_venta", exSaveReporte);
                throw exSaveReporte;
            }

            // 4. Actualizar el stock del producto (SALIDA)
            producto.setStockActual(producto.getStockActual() - venta.getCantidad());
            try {
                logger.debug("Actualizando stock producto id={} nuevoStock={}", producto.getIdProducto(), producto.getStockActual());
                productoRepository.saveAndFlush(producto);
            } catch (Exception exSaveProducto) {
                logger.error("Fallo al actualizar producto id={}", producto != null ? producto.getIdProducto() : null, exSaveProducto);
                throw exSaveProducto;
            }

            // 5. Registrar el movimiento (SALIDA de inventario)
            Movimiento movimiento = new Movimiento();
            movimiento.setProducto(producto);
            movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
            movimiento.setCantidad(venta.getCantidad());
            String clienteNombre = venta != null && venta.getCliente() != null ? venta.getCliente().getNombre() : "-";
            movimiento.setDescripcion("Venta a cliente " + clienteNombre
                    + " - " + venta.getTipoDocumento() + " #" + nuevaVenta.getIdVenta());
            movimiento.setFechaMovimiento(LocalDateTime.now());
            try {
                movimientoRepository.saveAndFlush(movimiento);
            } catch (Exception exSaveMovimiento) {
                logger.error("Fallo al guardar movimiento for productoId={}", producto != null ? producto.getIdProducto() : null, exSaveMovimiento);
                throw exSaveMovimiento;
            }

            return nuevaVenta;
        } catch (Exception ex) {
            logger.error("Error creando venta. clienteId={}, productoId={}, cantidad={}",
                    venta != null && venta.getCliente() != null ? venta.getCliente().getIdCliente() : null,
                    venta != null && venta.getProducto() != null ? venta.getProducto().getIdProducto() : null,
                    venta != null ? venta.getCantidad() : null, ex);
            throw ex;
        }
    }

    public List<ReporteVenta> listar() {
        return reporteVentaRepository.findAll().stream()
                .peek(this::calcularTotalesSiNecesario)
                .collect(Collectors.toList());
    }

    public List<ReporteVentaDTO> listarDTO() {
        return reporteVentaRepository.findAll().stream()
                .peek(this::calcularTotalesSiNecesario)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<ReporteVentaDTO> listarPaginado(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findAll(pageable);
        page.getContent().forEach(this::calcularTotalesSiNecesario);
        return convertPageToResponse(page);
    }

    public List<ReporteVenta> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return reporteVentaRepository.findByFechaVentaBetween(inicio, fin).stream()
                .peek(this::calcularTotalesSiNecesario)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<ReporteVentaDTO> obtenerPorRangoFechasPaginado(
            LocalDateTime inicio, LocalDateTime fin, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findByFechaVentaBetween(inicio, fin, pageable);
        page.getContent().forEach(this::calcularTotalesSiNecesario);
        return convertPageToResponse(page);
    }

    public List<ReporteVenta> obtenerVentasPorClientePaisa(Long idCliente) {
        return reporteVentaRepository.findByCliente(idCliente).stream()
                .peek(this::calcularTotalesSiNecesario)
                .collect(Collectors.toList());
    }

    public PaginatedResponse<ReporteVentaDTO> obtenerVentasPorClientePaginado(
            Long idCliente, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ReporteVenta> page = reporteVentaRepository.findByCliente(idCliente, pageable);
        page.getContent().forEach(this::calcularTotalesSiNecesario);
        return convertPageToResponse(page);
    }

    private void calcularTotalesSiNecesario(ReporteVenta venta) {
        if (venta.getSubtotal() == null || venta.getIgv() == null) {
            Double precio = venta.getPrecioUnitario() != null ? venta.getPrecioUnitario() : 0.0;
            Integer cantidad = venta.getCantidad() != null ? venta.getCantidad() : 0;
            Double subtotal = precio * cantidad;
            Double igv = Math.round(subtotal * 0.18 * 100.0) / 100.0;
            Double total = Math.round((subtotal + igv) * 100.0) / 100.0;
            venta.setSubtotal(subtotal);
            venta.setIgv(igv);
            if (venta.getTotal() == null) {
                venta.setTotal(total);
            }
        }
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
                .apellidosCliente(venta.getCliente().getApellidos())
                .emailCliente(venta.getCliente().getEmail())
                .idProducto(venta.getProducto().getIdProducto())
                .nombreProducto(venta.getProducto().getNombre())
                .cantidad(venta.getCantidad())
                .precioUnitario(venta.getPrecioUnitario())
                .subtotal(venta.getSubtotal())
                .igv(venta.getIgv())
                .total(venta.getTotal())
                .tipoDocumento(venta.getTipoDocumento())
                .comprobanteNumero(venta.getComprobanteNumero())
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

    /**
     * Obtener siguiente número de comprobante de forma segura (LOCK row).
     */
    private Long nextComprobanteNumber() {
        entityManager.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS comprobante_sequence (
                    id VARCHAR(50) PRIMARY KEY,
                    `last_value` BIGINT NOT NULL
                )
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT IGNORE INTO comprobante_sequence (id, `last_value`)
                VALUES ('comprobante', 0)
                """).executeUpdate();

        entityManager.createNativeQuery("""
                UPDATE comprobante_sequence
                SET `last_value` = LAST_INSERT_ID(`last_value` + 1)
                WHERE id = 'comprobante'
                """).executeUpdate();

        Object res = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
        if (res instanceof BigInteger) {
            return ((BigInteger) res).longValue();
        }
        if (res instanceof Number) {
            return ((Number) res).longValue();
        }
        return Long.valueOf(res.toString());
    }

    private void calcularTotales(ReporteVenta venta) {
        Double precio = venta.getPrecioUnitario() != null ? venta.getPrecioUnitario() : 0.0;
        Integer cantidad = venta.getCantidad() != null ? venta.getCantidad() : 0;
        Double subtotal = precio * cantidad;
        Double igv = Math.round(subtotal * 0.18 * 100.0) / 100.0;
        Double total = Math.round((subtotal + igv) * 100.0) / 100.0;
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
    }
}
