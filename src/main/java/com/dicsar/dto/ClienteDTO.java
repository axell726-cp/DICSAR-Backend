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
public class ClienteDTO {

    private Long idCliente;
    private String nombre;
    private String apellidos;
    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;
    private String email;
    private String razonSocial;
    private Boolean esEmpresa;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Nombre completo para convenience
    public String getNombreCompleto() {
        if (razonSocial != null && !razonSocial.isEmpty()) {
            return razonSocial;
        }
        return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
    }
}
