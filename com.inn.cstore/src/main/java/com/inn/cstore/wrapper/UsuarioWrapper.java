package com.inn.cstore.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioWrapper {

    private Integer id;
    private String nombre;
    private String email;
    private String numerocontacto;
    private String contraseña;
    private String status;
    private String rol; // nombre del rol: "admin" o "usuario"

    public UsuarioWrapper(Integer id, String nombre, String email,
                          String numerocontacto, String contraseña,
                          String status) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.numerocontacto = numerocontacto;
        this.contraseña = contraseña;
        this.status = status;
    }

    public UsuarioWrapper(Integer id, String nombre, String email,
                          String numerocontacto, String contraseña,
                          String status, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.numerocontacto = numerocontacto;
        this.contraseña = contraseña;
        this.status = status;
        this.rol = rol;
    }
}