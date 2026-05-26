package com.inn.cstore.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Categoria;
import com.inn.cstore.POJO.Producto;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.servicelmpl.ProductoServiceImpl;
import com.inn.cstore.wrapper.ProductoWrapper;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock private ProductoDao productoDao;
    @Mock private JwtFilter jwtFilter;

    @InjectMocks
    private ProductoServiceImpl productoService;

    // Datos de apoyo reutilizables
    private Producto productoExistente;
    private ProductoWrapper productoWrapper;

    // Mapa de request válido para addNewProduct
    private Map<String, String> requestValido() {
        return Map.of(
            "nombre_producto", "Polo Clásico",
            "descripcion", "Polo de algodón",
            "precio", "50",
            "stock", "20",
            "IdCategoria", "1"
        );
    }

    @BeforeEach
    void setUp() {
        Categoria cat = new Categoria();
        cat.setId(1);
        cat.setNombre("Ropa");

        productoExistente = new Producto();
        productoExistente.setId(1);
        productoExistente.setNombre("Polo Clásico");
        productoExistente.setPrice(50);
        productoExistente.setStock(20);
        productoExistente.setStatus("true");
        productoExistente.setCategoria(cat);

        productoWrapper = new ProductoWrapper(1, "Coca Cola", "Bebida", "activo", 10, 2, "Bebidas", 50);
    }

    // ═══════════════════════════════════════════════════════════════
    //  addNewProduct
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addNewProduct: no es admin → UNAUTHORIZED, sin llamada al DAO")
    void addNewProduct_noEsAdmin_retornaUnauthorized() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        ResponseEntity<String> response = productoService.addNewProduct(requestValido());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains(CstoreConstants.UNAUTHORIZED_ACCESS));
        verify(productoDao, never()).save(any());
    }

    @Test
    @DisplayName("addNewProduct: admin + falta 'nombre_producto' → BAD_REQUEST")
    void addNewProduct_adminSinNombreProducto_retornaBadRequest() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        // Mapa sin el campo nombre_producto
        Map<String, String> requestIncompleto = Map.of(
            "stock", "10",
            "precio", "30",
            "IdCategoria", "1"
        );

        ResponseEntity<String> response = productoService.addNewProduct(requestIncompleto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(CstoreConstants.INVALID_DATA));
        verify(productoDao, never()).save(any());
    }

    @Test
    @DisplayName("addNewProduct: admin + falta 'stock' → BAD_REQUEST")
    void addNewProduct_adminSinStock_retornaBadRequest() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        Map<String, String> requestSinStock = Map.of(
            "nombre_producto", "Pantalón",
            "precio", "80",
            "IdCategoria", "2"
        );

        ResponseEntity<String> response = productoService.addNewProduct(requestSinStock);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(productoDao, never()).save(any());
    }

    @Test
    @DisplayName("addNewProduct: admin + datos completos → OK + producto guardado")
    void addNewProduct_adminDatosCompletos_guardaYRetornaOK() {
        when(jwtFilter.isAdmin()).thenReturn(true);

        ResponseEntity<String> response = productoService.addNewProduct(requestValido());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Producto added Successfully"));
        verify(productoDao, times(1)).save(any(Producto.class));
    }

    // ═══════════════════════════════════════════════════════════════
    //  updateProduct
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateProduct: no es admin → UNAUTHORIZED")
    void updateProduct_noEsAdmin_retornaUnauthorized() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        ResponseEntity<String> response = productoService.updateProduct(Map.of(
            "id_producto", "1", "nombre_producto", "X", "stock", "5"
        ));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(productoDao, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct: admin + id inexistente → mensaje de error con OK")
    void updateProduct_adminProductoInexistente_retornaMensajeError() {
        // NOTA: el código actual retorna HttpStatus.OK cuando el producto no existe.
        // Esto es un bug — debería retornar NOT_FOUND. Se documenta aquí para visibilidad.
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productoDao.findById(99)).thenReturn(Optional.empty());

        Map<String, String> request = Map.of(
            "id_producto", "99",
            "nombre_producto", "NoExiste",
            "stock", "0"
        );

        ResponseEntity<String> response = productoService.updateProduct(request);

        // Comportamiento actual (documenta la deuda técnica)
        assertTrue(response.getBody().contains("Product id Does not exist"));
        verify(productoDao, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct: admin + producto existe → OK + producto actualizado")
    void updateProduct_adminProductoExiste_actualizaYRetornaOK() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productoDao.findById(1)).thenReturn(Optional.of(productoExistente));

        Map<String, String> request = Map.of(
            "id_producto", "1",
            "nombre_producto", "Polo Actualizado",
            "descripcion", "Nueva desc",
            "precio", "60",
            "stock", "15",
            "IdCategoria", "1"
        );

        ResponseEntity<String> response = productoService.updateProduct(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product Updated Successfully"));
        verify(productoDao, times(1)).save(any(Producto.class));
    }

    // ═══════════════════════════════════════════════════════════════
    //  deleteProduct
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteProduct: no es admin → UNAUTHORIZED, sin delete")
    void deleteProduct_noEsAdmin_retornaUnauthorized() {
        when(jwtFilter.isAdmin()).thenReturn(false);

        ResponseEntity<String> response = productoService.deleteProduct(1);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(productoDao, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProduct: admin + id inexistente → mensaje sin llamar deleteById")
    void deleteProduct_adminIdInexistente_noEliminaYRetornaMensaje() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productoDao.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<String> response = productoService.deleteProduct(99);

        assertTrue(response.getBody().contains("Product id does not exist"));
        verify(productoDao, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProduct: admin + id existe → OK + deleteById llamado")
    void deleteProduct_adminIdExiste_eliminaYRetornaOK() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productoDao.findById(1)).thenReturn(Optional.of(productoExistente));
        doNothing().when(productoDao).deleteById(1);

        ResponseEntity<String> response = productoService.deleteProduct(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product Deleted Successfully"));
        verify(productoDao, times(1)).deleteById(1);
    }

    // ═══════════════════════════════════════════════════════════════
    //  updateStatus
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateStatus: admin + producto existe → OK + updateProductStatus llamado")
    void updateStatus_adminProductoExiste_actualizaEstado() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(productoDao.findById(1)).thenReturn(Optional.of(productoExistente));

        Map<String, String> request = Map.of("id_producto", "1", "status", "false");
        ResponseEntity<String> response = productoService.updateStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productoDao, times(1)).updateProductStatus("false", 1);
    }

    // ═══════════════════════════════════════════════════════════════
    //  getAllProduct
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllProduct: sin filtros → OK con lista de productos")
    void getAllProduct_retornaListaCompleta() {
        when(productoDao.getAllProduct()).thenReturn(List.of(productoWrapper));

        ResponseEntity<List<ProductoWrapper>> response = productoService.getAllProduct();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Coca Cola", response.getBody().get(0).getNombre());
    }

    @Test
    @DisplayName("getAllProduct: excepción en DAO → INTERNAL_SERVER_ERROR con lista vacía")
    void getAllProduct_excepcionDAO_retornaError() {
        when(productoDao.getAllProduct()).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<List<ProductoWrapper>> response = productoService.getAllProduct();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    //  getByCategory
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getByCategory: categoría válida → OK con productos de esa categoría")
    void getByCategory_categoriaValida_retornaProductos() {
        when(productoDao.getProductByCategory(1)).thenReturn(List.of(productoWrapper));

        ResponseEntity<List<ProductoWrapper>> response = productoService.getByCategory(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ═══════════════════════════════════════════════════════════════
    //  getProductById
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getProductById: id existente → OK con wrapper del producto")
    void getProductById_idExistente_retornaWrapper() {
        when(productoDao.getProductById(1)).thenReturn(productoWrapper);

        ResponseEntity<ProductoWrapper> response = productoService.getProductById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
        assertEquals("Coca Cola", response.getBody().getNombre());
    }
}