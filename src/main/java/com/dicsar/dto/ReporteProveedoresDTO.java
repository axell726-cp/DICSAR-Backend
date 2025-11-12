package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteProveedoresDTO {
    private Long totalProveedores;
    private Long proveedoresActivos;
    private Long proveedoresInactivos;
    private Long totalProductosPorProveedores;
    private Double promedioProductosPorProveedor;
    private ProveedorConMasProductosDTO proveedorConMasProductos;
}
