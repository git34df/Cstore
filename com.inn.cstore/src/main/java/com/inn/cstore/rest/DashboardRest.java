package com.inn.cstore.rest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/Dashboard")
public interface DashboardRest {

    @GetMapping(path="/Detalles")
    ResponseEntity<Map<String,Object>> getCount();

}
