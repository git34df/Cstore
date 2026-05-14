package com.inn.cstore.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cstore.POJO.Comprobante;

public interface ComprobanteDao extends JpaRepository<Comprobante, Integer> {

    Optional<Comprobante> findByUuid(String uuid);

    Optional<Comprobante> findByVentaId(Integer ventaId);

}
