package com.inn.cstore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.inn.cstore.POJO.Venta;

public interface VentaDao extends JpaRepository<Venta, Integer> {

    List<Venta> getAllVentas();

    List<Venta> getVentasByUsuario(@Param("email") String email);

}
