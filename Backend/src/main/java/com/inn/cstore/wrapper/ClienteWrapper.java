package com.inn.cstore.wrapper;

import java.util.List;

public class ClienteWrapper {

    // ── Datos del cliente ─────────────────────────────────────
    private Integer id;
    private String nombre;
    private String email;
    private String ruc;
    private String razonSocial;
    private String telefono;
    private String direccion;

    // ── Resumen de compras (calculado desde facturas) ─────────
    private Integer totalFacturas;       // cuántas facturas tiene
    private Integer totalUnidades;       // suma de unidades compradas
    private Double  totalDinero;         // suma de totalConIgv

    // ── Historial de facturas (solo en modal de detalle) ──────
    private List<FacturaResumenWrapper> facturas;

    // ── Constructores ─────────────────────────────────────────
    public ClienteWrapper() {}

    public ClienteWrapper(Integer id, String nombre, String email,
                          String ruc, String razonSocial, String telefono, String direccion,
                          Integer totalFacturas, Integer totalUnidades, Double totalDinero) {
        this.id            = id;
        this.nombre        = nombre;
        this.email         = email;
        this.ruc           = ruc;
        this.razonSocial   = razonSocial;
        this.telefono      = telefono;
        this.direccion     = direccion;
        this.totalFacturas = totalFacturas;
        this.totalUnidades = totalUnidades;
        this.totalDinero   = totalDinero;
    }

    public ClienteWrapper(Integer id, String nombre, String email, String ruc, String razonSocial,
                          String telefono, String direccion, Integer totalFacturas, Integer totalUnidades,
                          Double totalDinero, List<FacturaResumenWrapper> facturas) {
        this.id            = id;
        this.nombre        = nombre;
        this.email         = email;
        this.ruc           = ruc;
        this.razonSocial   = razonSocial;
        this.telefono      = telefono;
        this.direccion     = direccion;
        this.totalFacturas = totalFacturas;
        this.totalUnidades = totalUnidades;
        this.totalDinero   = totalDinero;
        this.facturas      = facturas;
    }

    // ── Getters y Setters ─────────────────────────────────────
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getTotalFacturas() { return totalFacturas; }
    public void setTotalFacturas(Integer totalFacturas) { this.totalFacturas = totalFacturas; }

    public Integer getTotalUnidades() { return totalUnidades; }
    public void setTotalUnidades(Integer totalUnidades) { this.totalUnidades = totalUnidades; }

    public Double getTotalDinero() { return totalDinero; }
    public void setTotalDinero(Double totalDinero) { this.totalDinero = totalDinero; }

    public List<FacturaResumenWrapper> getFacturas() { return facturas; }
    public void setFacturas(List<FacturaResumenWrapper> facturas) { this.facturas = facturas; }

    // ── DTO interno para cada factura del cliente ─────────────
    public static class FacturaResumenWrapper {
        private Integer id;
        private String  uuid;
        private String  serie;
        private Integer correlativo;
        private Double  totalConIgv;
        private String  metodoPago;
        private String  creadoPor;

        public FacturaResumenWrapper() {}

        public FacturaResumenWrapper(Integer id, String uuid, String serie, Integer correlativo,
                                     Double totalConIgv, String metodoPago, String creadoPor) {
            this.id           = id;
            this.uuid         = uuid;
            this.serie        = serie;
            this.correlativo  = correlativo;
            this.totalConIgv  = totalConIgv;
            this.metodoPago   = metodoPago;
            this.creadoPor    = creadoPor;
        }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }

        public String getSerie() { return serie; }
        public void setSerie(String serie) { this.serie = serie; }

        public Integer getCorrelativo() { return correlativo; }
        public void setCorrelativo(Integer correlativo) { this.correlativo = correlativo; }

        public Double getTotalConIgv() { return totalConIgv; }
        public void setTotalConIgv(Double totalConIgv) { this.totalConIgv = totalConIgv; }

        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

        public String getCreadoPor() { return creadoPor; }
        public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    }

}

