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
    name = "Bill.getAllBills",
    query = "Select b from Bill b order by b.id desc"
)

@NamedQuery(
    name = "Bill.getBillByUserName",
    query = "Select b from Bill b where b.createdby=: username order by b.id desc"
)

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "factura")
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer id;

    // ── Identificación del comprobante ────────────────────────
    @Column(name = "uuid")
    private String uuid;

    // Serie: F001 para facturas, B001 para boletas
    @Column(name = "serie", length = 4, nullable = false)
    private String serie = "F001";

    // Correlativo: 00000001, 00000002, ...
    @Column(name = "correlativo", nullable = false)
    private Integer correlativo = 1;

    // ── Datos del cliente ─────────────────────────────────────
    @Column(name = "nombre")
    private String nombre;

    @Column(name = "email")
    private String email;

    @Column(name = "numerocontacto")
    private String numerocontacto;

    // RUC del cliente — obligatorio en factura
    @Column(name = "ruc_cliente", length = 11)
    private String rucCliente;

    // Razón social del cliente — obligatorio en factura
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    // Dirección del cliente — recomendado en factura
    @Column(name = "direccion_cliente", length = 250)
    private String direccionCliente;

    // ── Pago ──────────────────────────────────────────────────
    @Column(name = "metodo_pago")
    private String metodo_pago;

    // ── Montos con IGV 18% ────────────────────────────────────
    // Valor de venta sin IGV
    @Column(name = "subtotal", nullable = false)
    private Double subtotal = 0.0;

    // IGV 18% sobre el subtotal
    @Column(name = "igv", nullable = false)
    private Double igv = 0.0;

    // Total = subtotal + igv  (lo que paga el cliente)
    @Column(name = "total_con_igv", nullable = false)
    private Double totalConIgv = 0.0;

    // Total legacy (se mantiene por compatibilidad)
    @Column(name = "total")
    private Integer total;

    // ── Detalle ───────────────────────────────────────────────
    @Column(name = "productodetalle", columnDefinition = "json")
    private String productodetail;

    @Column(name = "creado_por")
    private String createdby;

}
