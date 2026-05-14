package com.inn.cstore.wrapper;

import lombok.Data;

@Data
public class ProductoWrapper {
  Integer id;
    String nombre;
    String descripcion;
    String estado;
    Integer precio;
    Integer categoriaId;
    String categoriaName;
    Integer stock;

    public ProductoWrapper() {
    }

    public ProductoWrapper(
        Integer id,
        String nombre,
        String descripcion,
        String estado,
        Integer precio,
        Integer categoriaId,
        String categoriaName,
        Integer stock
    ) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.categoriaName = categoriaName;
        this.stock=stock;
    }

    public ProductoWrapper (Integer id, String nombre){
        this.id=id;
        this.nombre=nombre;
    }

    public ProductoWrapper(Integer id,String nombre, String description, Integer price){
        this.id=id;
        this.nombre=nombre;
        this.descripcion=description;
        this.precio=price;
    }

    
    

}
