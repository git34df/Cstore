package com.inn.cstore.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inn.cstore.wrapper.ClienteWrapper;

@RequestMapping(path = "/Cliente")

public interface ClienteRest {

    // GET /Cliente/getAll
    @GetMapping(path = "/getAll")
    ResponseEntity<List<ClienteWrapper>> getAllClientes();

    // GET /Cliente/resumen/{id}
    @GetMapping(path = "/resumen/{id}")
    ResponseEntity<ClienteWrapper> getClienteResumen(@PathVariable Integer id);

    // POST /Cliente/add
    @PostMapping(path = "/add")
    ResponseEntity<String> addCliente(@RequestBody Map<String, Object> requestMap);

    // PUT /Cliente/update
    @PutMapping(path = "/update")
    ResponseEntity<String> updateCliente(@RequestBody Map<String, Object> requestMap);

    // DELETE /Cliente/delete/{id}
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteCliente(@PathVariable Integer id);

}
