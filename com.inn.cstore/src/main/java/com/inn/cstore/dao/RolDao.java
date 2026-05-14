package com.inn.cstore.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cstore.POJO.Rol;

public interface RolDao extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);

}
