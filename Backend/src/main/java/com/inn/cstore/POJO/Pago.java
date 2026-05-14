package com.inn.cstore.POJO;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@Table(name = "pago")
public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    // EFECTIVO, TARJETA, TRANSFERENCIA, YAPE, PLIN, etc.
    @Column(name = "metodo", nullable = false, length = 30)
    private String metodo;

    @Column(name = "monto", nullable = false)
    private Integer monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    // PENDIENTE, COMPLETADO, RECHAZADO
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    // Relación 1:1 con Venta
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

}
