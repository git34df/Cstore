package com.inn.cstore.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cstore.POJO.Cliente;

public interface ClienteDao extends JpaRepository<Cliente, Integer> {

    List<Cliente> getAllClientes();

    Optional<Cliente> findByEmail(String email);

}