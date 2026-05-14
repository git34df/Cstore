package com.inn.cstore.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inn.cstore.wrapper.VentaWrapper;

@RequestMapping(path = "/Venta")
public interface VentaRest {

    // POST /Venta/registrar
    // Body: { detalle:[{productoId, cantidad, precioUnitario}], total, nombreCliente,
    //         emailCliente, telefonoCliente, metodoPago, tipoComprobante }
    @PostMapping(path = "/registrar")
    ResponseEntity<String> registrarVenta(@RequestBody Map<String, Object> requestMap);

    // GET /Venta/getVentas
    @GetMapping(path = "/getVentas")
    ResponseEntity<List<VentaWrapper>> getVentas();

    // GET /Venta/getVenta/{id}
    @GetMapping(path = "/getVenta/{id}")
    ResponseEntity<VentaWrapper> getVentaById(@PathVariable Integer id);

    // POST /Venta/getPdf   Body: { uuid: "..." }
    @PostMapping(path = "/getPdf")
    ResponseEntity<byte[]> getPdf(@RequestBody Map<String, Object> requestMap);

    // POST /Venta/anular/{id}
    @PostMapping(path = "/anular/{id}")
    ResponseEntity<String> anularVenta(@PathVariable Integer id);

}
