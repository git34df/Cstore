package com.inn.cstore.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.inn.cstore.wrapper.ClienteWrapper;

public interface ClienteService {

    // Lista todos los clientes con su resumen de compras
    ResponseEntity<List<ClienteWrapper>> getAllClientes();

    // Detalle completo de un cliente (incluye historial de facturas)
    ResponseEntity<ClienteWrapper> getClienteResumen(Integer id);

    // Agregar cliente manualmente
    ResponseEntity<String> addCliente(Map<String, Object> requestMap);

    // Actualizar datos del cliente
    ResponseEntity<String> updateCliente(Map<String, Object> requestMap);

    // Eliminar cliente
    ResponseEntity<String> deleteCliente(Integer id);

}