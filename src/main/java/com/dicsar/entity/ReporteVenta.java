package com.dicsar.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    private Integer cantidad;
    private Double precioUnitario;
    private Double total;

    // Numeración de comprobante (se completará al crear la venta)
    private Long comprobanteNumero;

    // Subtotal antes del IGV
    private Double subtotal;

    // IGV aplicado
    private Double igv;

    private String tipoDocumento; // "Boleta", "Factura", etc.

    @Builder.Default
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Builder.Default
    private Boolean estado = true; // true = activa, false = anulada
}
