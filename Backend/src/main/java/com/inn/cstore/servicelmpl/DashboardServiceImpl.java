package com.inn.cstore.servicelmpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cstore.dao.BillDao;
import com.inn.cstore.dao.CategoriaDao;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.POJO.Bill;
import com.inn.cstore.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoriaDao categoriaDao;

    @Autowired
    ProductoDao productoDao;

    @Autowired
    BillDao billDao;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        Map<String, Object> map = new HashMap<>();
        
        // Métricas básicas (las que ya tenías)
        map.put("categoria", categoriaDao.count());
        map.put("producto", productoDao.count());
        map.put("Facturas", billDao.count());
        
        // NUEVAS MÉTRICAS CALCULADAS
        
        // 1. Total de ingresos (suma de todos los totales de facturas)
        List<Bill> allBills = billDao.findAll();
        Integer totalIngresos = allBills.stream()
            .mapToInt(bill -> bill.getTotal() != null ? bill.getTotal() : 0)
            .sum();
        map.put("totalIngresos", totalIngresos);
        
        // 2. Stock total de productos
        Long stockTotal = productoDao.findAll().stream()
            .mapToLong(producto -> producto.getStock() != null ? producto.getStock() : 0L)
            .sum();
        map.put("stockTotal", stockTotal);
        
        // 3. Precio promedio de productos
        Double precioPromedio = productoDao.findAll().stream()
            .mapToDouble(producto -> producto.getPrice() != null ? producto.getPrice() : 0.0)
            .average()
            .orElse(0.0);
        map.put("precioPromedio", precioPromedio);
        
        // 4 y 5. Análisis de productos vendidos desde el JSON
        Map<String, ProductoVentas> ventasPorProducto = calcularVentasPorProducto(allBills);
        
        // Top 5 productos por unidades vendidas
        List<Map<String, Object>> topProductosUnidades = ventasPorProducto.values().stream()
            .sorted((p1, p2) -> Integer.compare(p2.getUnidades(), p1.getUnidades()))
            .limit(5)
            .map(pv -> {
                Map<String, Object> item = new HashMap<>();
                item.put("nombre", pv.getNombre());
                item.put("unidades", pv.getUnidades());
                return item;
            })
            .collect(Collectors.toList());
        map.put("topProductosUnidades", topProductosUnidades);
        
        // Top 5 productos por ingresos generados
        List<Map<String, Object>> topProductosIngresos = ventasPorProducto.values().stream()
            .sorted((p1, p2) -> Double.compare(p2.getIngresos(), p1.getIngresos()))
            .limit(5)
            .map(pv -> {
                Map<String, Object> item = new HashMap<>();
                item.put("nombre", pv.getNombre());
                item.put("ingresos", pv.getIngresos());
                return item;
            })
            .collect(Collectors.toList());
        map.put("topProductosIngresos", topProductosIngresos);

        return new ResponseEntity<>(map, HttpStatus.OK);
    }
    
    /**
     * Procesa el JSON de productodetalle de todas las facturas
     * y calcula las ventas por producto
     */
    private Map<String, ProductoVentas> calcularVentasPorProducto(List<Bill> bills) {
        Map<String, ProductoVentas> ventasMap = new HashMap<>();
        
        for (Bill bill : bills) {
            if (bill.getProductodetail() == null || bill.getProductodetail().isEmpty()) {
                continue;
            }
            
            try {
                // Parsear el JSON de productos
                // Formato esperado: [{"nombre":"Producto A","categoria":"Cat1","cantidad":2,"precio":50,"total":100}, ...]
                List<Map<String, Object>> productos = objectMapper.readValue(
                    bill.getProductodetail(), 
                    new TypeReference<List<Map<String, Object>>>(){}
                );
                
                for (Map<String, Object> producto : productos) {
                    String nombre = (String) producto.get("nombre");
                    Integer cantidad = getIntegerValue(producto.get("cantidad"));
                    Double precio = getDoubleValue(producto.get("precio"));
                    Double total = getDoubleValue(producto.get("total"));
                    
                    // Si no hay total, calcularlo
                    if (total == null || total == 0) {
                        total = cantidad * precio;
                    }
                    
                    // Agregar o actualizar en el map
                    ProductoVentas pv = ventasMap.getOrDefault(nombre, new ProductoVentas(nombre));
                    pv.agregarVenta(cantidad, total);
                    ventasMap.put(nombre, pv);
                }
                
            } catch (Exception e) {
                System.err.println("Error al parsear productodetail: " + e.getMessage());
                // Continuar con la siguiente factura
            }
        }
        
        return ventasMap;
    }
    
    /**
     * Helpers para convertir valores del JSON de manera segura
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private Double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Clase interna para almacenar las ventas de un producto
     */
    private static class ProductoVentas {
        private String nombre;
        private int unidades;
        private double ingresos;
        
        public ProductoVentas(String nombre) {
            this.nombre = nombre;
            this.unidades = 0;
            this.ingresos = 0.0;
        }
        
        public void agregarVenta(int cantidad, double total) {
            this.unidades += cantidad;
            this.ingresos += total;
        }
        
        public String getNombre() { return nombre; }
        public int getUnidades() { return unidades; }
        public double getIngresos() { return ingresos; }
    }
}