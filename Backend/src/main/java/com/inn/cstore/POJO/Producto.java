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


@NamedQuery(
    name = "Producto.getAllProduct",
    query = "select new com.inn.cstore.wrapper.ProductoWrapper(p.id, p.nombre, p.description, p.status, p.price, p.categoria.id, p.categoria.nombre,p.stock) from Producto p"
)

@NamedQuery(
    name = "Producto.updateProductStatus",
    query = "update Producto p set p.status=:status where p.id=:id"
)

@NamedQuery(
    name = "Producto.getProductByCategory",
    query = "select new com.inn.cstore.wrapper.ProductoWrapper(p.id,p.nombre) from Producto p where p.categoria.id=:id and p.status='true'"
)

@NamedQuery(
    name="Producto.getProductById",
    query="select new com.inn.cstore.wrapper.ProductoWrapper(p.id,p.nombre,p.description,p.price) from Producto p where p.id=:id"
)

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="producto")
public class Producto implements Serializable{

    public static final long serialVersionUid=12356L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_producto")
    private Integer id;

    @Column(name = "nombreProducto")
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Categoria.fk", nullable = false)
    private Categoria categoria;
    

    @Column(name="Descripcion")
    private String description;

    @Column(name="precio")
    private Integer price;
    
    @Column(name="estado")
    private String status;

    @Column(name="stock")
    private Integer stock;


}
