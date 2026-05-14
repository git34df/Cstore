package com.inn.cstore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cstore.POJO.Categoria;

public interface CategoriaDao extends JpaRepository<Categoria,Integer> {

    List<Categoria> getAllCategoria();

}
