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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "comprobante")
public class Comprobante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer id;

    // BOLETA o FACTURA
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    // UUID para identificar el PDF generado
    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "email_cliente")
    private String emailCliente;

    @Column(name = "telefono_cliente", length = 20)
    private String telefonoCliente;

    // Relación 1:1 con Venta
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

}
