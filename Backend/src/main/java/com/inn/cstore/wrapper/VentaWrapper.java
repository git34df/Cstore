package com.inn.cstore.wrapper;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VentaWrapper {

    private Integer id;
    private LocalDateTime fecha;
    private Integer total;
    private String estado;
    private String usuarioEmail;

    // Datos del comprobante (nullable si aún no se emitió)
    private String comprobanteUuid;
    private String comprobanteTipo;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;

    // Datos del pago (nullable si aún no se procesó)
    private String pagoMetodo;
    private String pagoEstado;

    // Líneas del detalle
    private List<VentaDetalleWrapper> detalle;

}
