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
            // 1. Validar que el producto existe
            Producto producto = productoRepository.findById(venta.getProducto().getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            // 2. Validar que hay stock suficiente
            if (venta.getCantidad() > producto.getStockActual()) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStockActual());
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
                nuevaVenta = reporteVentaRepository.save(venta);
            } catch (Exception exSaveReporte) {
                logger.error("Fallo al guardar reporte_venta", exSaveReporte);
                throw exSaveReporte;
            }

            // 4. Actualizar el stock del producto (SALIDA)
            producto.setStockActual(producto.getStockActual() - venta.getCantidad());
            try {
                logger.debug("Actualizando stock producto id={} nuevoStock={}", producto.getIdProducto(), producto.getStockActual());
                productoRepository.save(producto);
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
                movimientoRepository.save(movimiento);
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

    /**
     * Obtener siguiente número de comprobante de forma segura (LOCK row).
     */
    private Long nextComprobanteNumber() {
        try {
            Object res = entityManager.createNativeQuery("SELECT last_value FROM comprobante_sequence WHERE id = 'comprobante' FOR UPDATE").getSingleResult();
            Long current = 0L;
            if (res instanceof BigInteger) {
                current = ((BigInteger) res).longValue();
            } else if (res instanceof Number) {
                current = ((Number) res).longValue();
            }
            Long next = current + 1;
            entityManager.createNativeQuery("UPDATE comprobante_sequence SET last_value = ? WHERE id = 'comprobante'")
                    .setParameter(1, next)
                    .executeUpdate();
            return next;
        } catch (Exception ex) {
            // Fallback: intentar insertar fila y retornar 1
            try {
                entityManager.createNativeQuery("INSERT INTO comprobante_sequence (id, last_value) VALUES ('comprobante', 1)").executeUpdate();
                return 1L;
            } catch (Exception ignored) {
            }
            return 1L;
        }
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
