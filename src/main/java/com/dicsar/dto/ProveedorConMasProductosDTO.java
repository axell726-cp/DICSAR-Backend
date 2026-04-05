package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorConMasProductosDTO {
    private Long idProveedor;
    private String razonSocial;
    private String ruc;
    private Long cantidadProductos;
}
