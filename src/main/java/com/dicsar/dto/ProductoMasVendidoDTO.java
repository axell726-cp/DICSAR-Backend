package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoMasVendidoDTO {
    private Long idProducto;
    private String nombreProducto;
    private Long cantidadVendida;
    private Double montoTotal;
}
