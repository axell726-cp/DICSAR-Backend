package com.dicsar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricasDTO {
    private Long totalVentas;
    private Double montoTotalVentas;
    private Long totalClientes;
    private Long totalProductos;
    private Long productosAgotados;
    private Double ventasHoy;
    private Double ventasEstaSemana;
    private Double ventasEsteMes;
}
