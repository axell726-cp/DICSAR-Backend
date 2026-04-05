package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteInventarioDTO {
    private Long totalProductos;
    private Long productosActivos;
    private Long productosInactivos;
    private Long productosConStockBajo;
    private Long productosSinStock;
    private Double valorTotalInventario;
    private Long totalCategorias;
    private Integer stockTotalActual;
}
