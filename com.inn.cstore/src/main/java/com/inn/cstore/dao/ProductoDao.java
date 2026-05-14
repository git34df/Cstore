package com.inn.cstore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.inn.cstore.POJO.Producto;
import com.inn.cstore.wrapper.ProductoWrapper;

public interface ProductoDao extends JpaRepository<Producto, Integer> {

    List<ProductoWrapper> getAllProduct();

    
    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id_producto);

    List<ProductoWrapper> getProductByCategory(@Param("id") Integer id);

    ProductoWrapper getProductById(@Param("id") Integer id);
    Producto findByNombre(String nombre);

}
