package com.inn.cstore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.inn.cstore.POJO.VentaDetalle;

public interface VentaDetalleDao extends JpaRepository<VentaDetalle, Integer> {

    List<VentaDetalle> getDetalleByVenta(@Param("ventaId") Integer ventaId);

}
