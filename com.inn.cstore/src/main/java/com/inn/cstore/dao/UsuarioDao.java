package com.inn.cstore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.inn.cstore.POJO.Usuario;
import com.inn.cstore.wrapper.UsuarioWrapper;

import jakarta.transaction.Transactional;

public interface UsuarioDao extends JpaRepository<Usuario, Integer> {

    Usuario findByEmail(@Param("email") String email);

    List<UsuarioWrapper> getAllUsuarios();

    List<String> getAllAdmin();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("estado") String estado, @Param("id") Integer id);

    @Transactional
    @Modifying
    Integer updateRol(@Param("rolId") Integer rolId, @Param("id") Integer id);

}