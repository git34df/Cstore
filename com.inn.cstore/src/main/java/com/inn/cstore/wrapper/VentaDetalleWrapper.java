package com.inn.cstore.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VentaDetalleWrapper {

    private Integer productoId;
    private String productoNombre;
    private String categoriaNombre;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;

    // Constructor para lectura (respuesta al frontend)
    public VentaDetalleWrapper(Integer productoId, String productoNombre,
                                String categoriaNombre, Integer cantidad,
                                Integer precioUnitario, Integer subtotal) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.categoriaNombre = categoriaNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }
}
