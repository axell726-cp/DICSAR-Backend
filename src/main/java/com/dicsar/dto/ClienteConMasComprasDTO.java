package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteConMasComprasDTO {
    private Long idCliente;
    private String nombreCliente;
    private Long cantidadVentas;
    private Double montoTotal;
}
