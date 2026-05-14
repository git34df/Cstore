package com.inn.cstore.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.inn.cstore.wrapper.VentaWrapper;

public interface VentaService {

    // Registra la venta + detalle + descuenta stock, genera comprobante y pago
    ResponseEntity<String> registrarVenta(Map<String, Object> requestMap);

    // Devuelve todas las ventas (admin) o las del usuario autenticado
    ResponseEntity<List<VentaWrapper>> getVentas();

    // Devuelve una venta completa con su detalle, comprobante y pago
    ResponseEntity<VentaWrapper> getVentaById(Integer id);

    // Genera / descarga el PDF del comprobante
    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    // Anula una venta (no la elimina físicamente)
    ResponseEntity<String> anularVenta(Integer id);

}
