package com.dicsar.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dicsar.entity.Movimiento;
import com.dicsar.entity.Producto;
import com.dicsar.entity.Usuario;
import com.dicsar.enums.TipoMovimiento;
import com.dicsar.service.MovimientoService;
import com.dicsar.service.ProductoService;
import com.dicsar.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    // Registrar un nuevo movimiento
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Movimiento> crearMovimiento(@RequestBody Movimiento movimiento,
            @RequestParam String usuario) {
        // Validar que el producto exista
        Producto producto = productoService.obtenerPorId(movimiento.getProducto().getIdProducto());
        movimiento.setProducto(producto);

        // Buscar el usuario y asociarlo como entidad
        Usuario usuarioEntity = usuarioService.buscarPorUsername(usuario);
        movimiento.setUsuario(usuarioEntity);

        // Registrar el movimiento
        Movimiento nuevoMovimiento = movimientoService.crearMovimiento(movimiento, usuario);

        // Verificar si el stock quedó bajo y crear notificación
        productoService.verificarYCrearNotificacionStock(producto.getIdProducto(), usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMovimiento);
    }

    // Listar todos los movimientos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Movimiento>> listarMovimientos() {
        List<Movimiento> movimientos = movimientoService.listarTodos();
        return ResponseEntity.ok(movimientos);
    }

    // Listar movimientos paginados
    @GetMapping("/pagina/todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Page<Movimiento>> listarPaginados(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "ENTRADA") TipoMovimiento tipo) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("fechaMovimiento").descending());
        Page<Movimiento> movimientos = movimientoService.listarPaginados(pageable);
        return ResponseEntity.ok(movimientos);
    }

    // Listar movimientos de un producto específico
    @GetMapping("/producto/{idProducto}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Movimiento>> listarPorProducto(@PathVariable Long idProducto) {
        productoService.obtenerPorId(idProducto); // valida que el producto exista
        List<Movimiento> movimientos = movimientoService.listarPorProducto(idProducto);
        return ResponseEntity.ok(movimientos);
    }

    // Filtrar movimientos por tipo
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Movimiento>> filtrarPorTipo(@PathVariable TipoMovimiento tipo) {
        List<Movimiento> movimientos = movimientoService.listarPorTipo(tipo);
        return ResponseEntity.ok(movimientos);
    }

    // Filtrar por rango de fechas
    @GetMapping("/fecha")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<Movimiento>> filtrarPorFecha(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        List<Movimiento> movimientos = movimientoService.listarPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(movimientos);
    }

    // Obtener movimiento por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Movimiento> obtenerMovimiento(@PathVariable Long id) {
        Movimiento movimiento = movimientoService.obtenerPorId(id);
        return movimiento != null ? ResponseEntity.ok(movimiento) : ResponseEntity.notFound().build();
    }

    // 🆕 NUEVO: Obtener precio según tipo de movimiento y producto
    @GetMapping("/precio")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<?> obtenerPrecio(
            @RequestParam Long idProducto,
            @RequestParam String tipo) {

        Producto producto = productoService.obtenerPorId(idProducto);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idProducto", idProducto);
        respuesta.put("nombreProducto", producto.getNombre());
        respuesta.put("tipoMovimiento", tipo);

        Double precio = 0.0;
        String fuente = "";

        switch (tipo.toUpperCase()) {
            case "ENTRADA":
                precio = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : 0.0;
                fuente = "precio_compra";
                break;
            case "SALIDA":
                precio = producto.getPrecio() != null ? producto.getPrecio() : 0.0;
                fuente = "precio_venta";
                break;
            case "AJUSTE":
                precio = producto.getPrecio() != null ? producto.getPrecio() : 0.0;
                fuente = "precio_venta";
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }

        respuesta.put("precio", precio);
        respuesta.put("fuente", fuente);

        return ResponseEntity.ok(respuesta);
    }
}
