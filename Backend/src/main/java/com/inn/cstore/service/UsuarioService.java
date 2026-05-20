package com.inn.cstore.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.inn.cstore.wrapper.UsuarioWrapper;

public interface UsuarioService {

    ResponseEntity<String> singUp(Map<String, String> requestMap);
    ResponseEntity<String> Login(Map<String, String> requestMap);
    ResponseEntity<List<UsuarioWrapper>> getAllUsuarios();
    ResponseEntity<String> update(Map<String, String> requestMap);
    ResponseEntity<String> checkToken();
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
    ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
    ResponseEntity<String> updateRol(Map<String, String> requestMap);
    ResponseEntity<String> resetPassword(Map<String, String> requestMap);
}
