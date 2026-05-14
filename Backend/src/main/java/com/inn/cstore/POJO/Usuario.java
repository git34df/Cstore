package com.inn.cstore.POJO;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@NamedQuery(name = "Usuario.findByEmail",
        query = "select u from Usuario u where u.email =:email")

// Devuelve todos los usuarios con su rol incluido
@NamedQuery(name = "Usuario.getAllUsuarios",
        query = "select new com.inn.cstore.wrapper.UsuarioWrapper(" +
                "u.id, u.nombre, u.email, u.numerotelefono, u.password, u.estado, u.rol.nombre) " +
                "from Usuario u")

@NamedQuery(name = "Usuario.updateStatus",
        query = "update Usuario u set u.estado =:estado where u.id =:id")

@NamedQuery(name = "Usuario.updateRol",
        query = "update Usuario u set u.rol.id =:rolId where u.id =:id")

@NamedQuery(name = "Usuario.getAllAdmin",
        query = "select u.email from Usuario u where u.rol.nombre = 'admin'")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "numerotelefono")
    private String numerotelefono;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "estado")
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

}