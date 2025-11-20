package com.dicsar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dicsar.dto.NotificacionDTO;
import com.dicsar.entity.Notificacion;
import com.dicsar.entity.Producto;
import com.dicsar.enums.NivelAlerta;
import com.dicsar.enums.TipoAlerta;
import com.dicsar.repository.NotificacionRepository;
import com.dicsar.repository.ProductoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ProductoRepository productoRepository;

    // 🔹 Verificar si ya existe una notificación activa
    public boolean existeNotificacionActiva(Long productoId, TipoAlerta tipo) {
        if (productoId == null)
            return false;
        return notificacionRepository.existsByProductoIdProductoAndTipo(productoId, tipo);
    }

    // 🔹 Guardar notificación genérica
    public void notificarEvento(Producto producto,
            TipoAlerta tipo,
            NivelAlerta nivel,
            String titulo,
            String mensaje,
            String descripcion,
            String usuario) {

        Notificacion n = Notificacion.builder()
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .nivel(nivel)
                .descripcion(descripcion)
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .producto(producto)
                .build();

        notificacionRepository.save(n);
    }

    // 🔹 Notificación de vencimiento próximo
    public Notificacion notificarVencimientoProximo(Producto producto, long dias, String usuario) {
        Notificacion n = Notificacion.builder()
                .titulo("Producto próximo a vencer")
                .mensaje("El producto " + producto.getNombre() + " vencerá en " + dias + " días.")
                .tipo(TipoAlerta.ALERTA_VENCIMIENTO)
                .nivel(NivelAlerta.ADVERTENCIA)
                .descripcion("Producto dentro de los próximos 30 días de vencimiento.")
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .producto(producto)
                .build();

        notificacionRepository.save(n);
        return n;
    }

    // 🔹 Notificación de vencimiento expirado
    public Notificacion notificarVencimientoExpirado(Producto producto, String usuario) {
        Notificacion n = Notificacion.builder()
                .titulo("Producto vencido")
                .mensaje("El producto " + producto.getNombre() + " ha vencido y no puede ser comercializado.")
                .tipo(TipoAlerta.ALERTA_VENCIMIENTO)
                .nivel(NivelAlerta.CRITICA)
                .descripcion("Producto vencido detectado por el sistema.")
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .producto(producto)
                .build();

        notificacionRepository.save(n);
        return n;
    }

    // 🔹 Notificación de stock mínimo
    public Notificacion notificarStockMinimo(Producto producto, String usuario) {
        Notificacion n = Notificacion.builder()
                .titulo("Stock mínimo alcanzado")
                .mensaje("El producto " + producto.getNombre() + " ha alcanzado su stock mínimo.")
                .tipo(TipoAlerta.STOCK_BAJO)
                .nivel(NivelAlerta.ADVERTENCIA)
                .descripcion("El stock actual es igual o menor al stock mínimo configurado.")
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .producto(producto)
                .build();

        notificacionRepository.save(n);
        return n;
    }

    // 🔹 Notificación de cambio de precio
    public Notificacion notificarCambioPrecio(Producto producto, Double precioAnterior, Double precioNuevo,
            String descripcion, String usuario) {
        Notificacion n = Notificacion.builder()
                .titulo("Cambio de precio")
                .mensaje("El precio del producto " + producto.getNombre() + " cambió de S/ "
                        + String.format("%.2f", precioAnterior) + " a S/ " + String.format("%.2f", precioNuevo))
                .tipo(TipoAlerta.PRECIO)
                .nivel(NivelAlerta.INFORMATIVA)
                .descripcion(descripcion)
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .producto(producto)
                .build();

        notificacionRepository.save(n);
        return n;
    }

    // 🔹 Método para generar alertas por vencimiento
    public List<Notificacion> generarAlertasPorVencimiento(Producto producto, String usuario) {
        List<Notificacion> alertas = new ArrayList<>();

        if (producto.getFechaVencimiento() != null) {
            LocalDate hoy = LocalDate.now();
            LocalDate venc = producto.getFechaVencimiento();

            long diasRestantes = ChronoUnit.DAYS.between(hoy, venc);

            if (diasRestantes < 0) {
                alertas.add(notificarVencimientoExpirado(producto, usuario));
            } else if (diasRestantes <= 10) {
                alertas.add(notificarVencimientoProximo(producto, diasRestantes, usuario));
            }
        }

        return alertas;
    }

    // 🔹 Listar todas las notificaciones
    public List<Notificacion> listar() {
        return notificacionRepository.findAll();
    }

    // 🔹 Listar notificaciones paginadas
    public Page<Notificacion> listarPaginadas(Pageable pageable) {
        return notificacionRepository.findAll(pageable);
    }

    // 🔹 Listar notificaciones no leídas
    public List<Notificacion> listarNoLeidas() {
        return notificacionRepository.findByLeidoFalse();
    }

    // 🔹 Listar por tipo
    public List<Notificacion> listarPorTipo(TipoAlerta tipo) {
        return notificacionRepository.findByTipo(tipo);
    }

    // 🔹 Listar por producto
    public List<Notificacion> listarPorProducto(Long idProducto) {
        Producto p = productoRepository.findById(idProducto).orElseThrow();
        return notificacionRepository.findByProducto(p);
    }

    // 🔹 Marcar como leída
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id).orElse(null);
        if (notificacion != null) {
            notificacion.setLeido(true);
            notificacionRepository.save(notificacion);
        }
        return notificacion;
    }

    // 🔹 Marcar todas como leídas
    public void marcarTodasComoLeidas() {
        List<Notificacion> notificaciones = notificacionRepository.findByLeidoFalse();
        notificaciones.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(notificaciones);
    }

    // 🔹 Mapear entidades a DTO
    public List<NotificacionDTO> mapearADTO(List<Notificacion> notificaciones) {
        return notificaciones.stream()
                .map(n -> NotificacionDTO.builder()
                        .titulo(n.getTitulo())
                        .mensaje(n.getMensaje())
                        .descripcion(n.getDescripcion())
                        .tipo(n.getTipo())
                        .nivel(n.getNivel())
                        .fechaHora(n.getFechaHora())
                        .build())
                .collect(Collectors.toList());
    }

    // 🔹 Guardar notificación directamente
    public void guardar(Notificacion notificacion) {
        notificacionRepository.save(notificacion);
    }

    // 🔹 Eliminar notificación por ID
    public void eliminar(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró la notificación con ID: " + id);
        }
        notificacionRepository.deleteById(id);
    }

    // 🔹 Eliminar todas las notificaciones
    public void eliminarTodas() {
        notificacionRepository.deleteAll();
    }

    // 🔹 Revisión automática diaria de vencimientos (2:00 AM)
    @Scheduled(cron = "0 0 2 * * *")
    public void revisarVencimientosAutomaticamente() {
        List<Producto> productos = productoRepository.findAll();

        for (Producto p : productos) {
            if (p.getFechaVencimiento() != null) {
                generarAlertasPorVencimiento(p, "sistema");
            }
        }
        System.out.println("✅ Revisión automática de vencimientos ejecutada: " + LocalDate.now());
    }

}
