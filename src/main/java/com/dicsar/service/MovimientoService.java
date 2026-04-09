package com.dicsar.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.dicsar.entity.Movimiento;
import com.dicsar.entity.Producto;
import com.dicsar.entity.Usuario;
import com.dicsar.enums.TipoMovimiento;
import com.dicsar.repository.MovimientoRepository;
import com.dicsar.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoService {

	private final MovimientoRepository movimientoRepository;
	private final UsuarioRepository usuarioRepository;

	public void registrarMovimiento(Producto producto, int stockAnterior, int stockNuevo, String usuario) {
		if (Objects.equals(stockAnterior, stockNuevo))
			return;

		int diferencia = stockNuevo - stockAnterior;

		TipoMovimiento tipo = diferencia > 0 ? TipoMovimiento.ENTRADA
				: diferencia < 0 ? TipoMovimiento.SALIDA : TipoMovimiento.AJUSTE;

		Usuario usuarioEntity = usuarioRepository.findByUsername(usuario).orElse(null);

		Movimiento mov = Movimiento.builder()
				.producto(producto)
				.tipoMovimiento(tipo)
				.cantidad(Math.abs(diferencia))
				.descripcion("Cambio de stock: " + stockAnterior + " → " + stockNuevo)
				.usuario(usuarioEntity)
				.fechaMovimiento(LocalDateTime.now())
				.build();

		movimientoRepository.save(mov);
	}

	@Transactional
	public Movimiento crearMovimiento(Movimiento movimiento, String usuario) {
		Producto producto = movimiento.getProducto();
		int stockActual = producto.getStockActual();
		int cantidad = movimiento.getCantidad();

		switch (movimiento.getTipoMovimiento()) {
			case ENTRADA -> {
				producto.setStockActual(stockActual + cantidad);
			}
			case SALIDA -> {
				if (stockActual < cantidad) {
					throw new IllegalArgumentException("Stock insuficiente para realizar la salida.");
				}
				producto.setStockActual(stockActual - cantidad);
			}
			case AJUSTE -> {
				producto.setStockActual(cantidad);
			}
		}

		producto.setFechaActualizacion(LocalDateTime.now());
		movimiento.setFechaMovimiento(LocalDateTime.now());
		Movimiento nuevoMovimiento = movimientoRepository.save(movimiento);

		// Verificar si el stock quedó bajo después de la operación y crear notificación
		if (producto.getStockMinimo() != null && producto.getStockActual() <= producto.getStockMinimo()) {
			// La creación de notificación se maneja en ProductoService
		}

		return nuevoMovimiento;
	}

	public List<Movimiento> listarTodos() {
		return movimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaMovimiento"));
	}

	public Page<Movimiento> listarPaginados(Pageable pageable) {
		return movimientoRepository.findAll(pageable);
	}

	public List<Movimiento> listarPorProducto(Long idProducto) {
		return movimientoRepository.findByProductoIdProductoOrderByFechaMovimientoDesc(idProducto);
	}

	public List<Movimiento> listarPorTipo(TipoMovimiento tipo) {
		return movimientoRepository.findByTipoMovimiento(tipo);
	}

	public List<Movimiento> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
		return movimientoRepository.findByFechaMovimientoBetween(inicio, fin);
	}

	public Movimiento obtenerPorId(Long id) {
		Optional<Movimiento> mov = movimientoRepository.findById(id);
		return mov.orElse(null);
	}

}
