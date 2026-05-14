package com.inn.cstore.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cstore.POJO.Pago;

public interface PagoDao extends JpaRepository<Pago, Integer> {

    Optional<Pago> findByVentaId(Integer ventaId);

}
