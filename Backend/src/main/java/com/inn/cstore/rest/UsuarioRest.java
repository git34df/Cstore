package com.inn.cstore.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inn.cstore.wrapper.UsuarioWrapper;

@RequestMapping(path = "/usuario")
public interface UsuarioRest {

    @PostMapping(path = "/signup")
    ResponseEntity<String> SingUp(@RequestBody(required = true) Map<String, String> RequestMap);

    @PostMapping(path = "/Login")
    ResponseEntity<String> Login(@RequestBody(required = true) Map<String, String> RequestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<UsuarioWrapper>> getAllUsuarios();

    @PostMapping(path = "/Update")
    ResponseEntity<String> update(@RequestBody(required = true) Map<String, String> RequestMap);

    // Nuevo: cambiar rol (solo admin)
    @PostMapping(path = "/updateRol")
    ResponseEntity<String> updateRol(@RequestBody(required = true) Map<String, String> RequestMap);

    @GetMapping(path = "/checkToken")
    ResponseEntity<String> checkToken();

    @PostMapping(path = "/ChangePassword")
    ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path = "/ForgotPassword")
    ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> requestMap);

    @PostMapping(path = "/resetPassword")
    ResponseEntity<String> resetPassword(@RequestBody Map<String, String> requestMap);
}