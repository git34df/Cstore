package com.inn.cstore.POJO;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@NamedQuery(
    name = "Categoria.getAllCategoria",
    query = "SELECT c FROM Categoria c WHERE c.id IN (SELECT p.categoria.id FROM Producto p WHERE p.status = 'true')"
)

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="Categoria")
public class Categoria implements Serializable  {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="IdCategoria")
    private Integer id;

    @Column(name="nombre")
    private String nombre;



}
