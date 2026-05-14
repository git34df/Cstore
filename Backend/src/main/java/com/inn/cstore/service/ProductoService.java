package com.inn.cstore.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.inn.cstore.wrapper.ProductoWrapper;

public interface ProductoService {

    ResponseEntity<String> addNewProduct(Map<String, String> requestMap);

    ResponseEntity<List<ProductoWrapper>> getAllProduct();

    ResponseEntity<String> updateProduct(Map <String, String> RequestMap);

    ResponseEntity<String> deleteProduct(Integer id);

    ResponseEntity<String> updateStatus(Map<String,String> RequestMap);

    ResponseEntity<List<ProductoWrapper>> getByCategory(Integer id);

    ResponseEntity<ProductoWrapper> getProductById(Integer id);

}
