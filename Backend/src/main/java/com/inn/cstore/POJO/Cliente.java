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
    name = "Cliente.getAllClientes",
    query = "select c from Cliente c order by c.nombre asc"
)

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "cliente")
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "ruc", length = 11)
    private String ruc;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 300)
    private String direccion;

}
