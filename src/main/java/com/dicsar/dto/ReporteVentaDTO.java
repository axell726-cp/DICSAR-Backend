package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteVentaDTO {

    private Long idVenta;
    private Long idCliente;
    private String nombreCliente;
    private String emailCliente;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double total;
    private String tipoDocumento;
    private LocalDateTime fechaVenta;
    private Boolean estado;
}
