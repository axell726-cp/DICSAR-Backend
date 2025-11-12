package com.dicsar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dicsar.enums.EstadoVencimiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {
    private Long idProducto;
    private String nombre;
    private String codigo;
    private String descripcion;
    private Double precioBase;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean estado;
    private LocalDate fechaVencimiento;
    private EstadoVencimiento estadoVencimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Datos de relaciones simplificados
    private Long categoriaId;
    private String categoriaNombre;
    
    private Long unidadMedidaId;
    private String unidadMedidaNombre;
    private String unidadMedidaAbreviatura;
    
    private Long proveedorId;
    private String proveedorNombre;
}
