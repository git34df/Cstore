package com.inn.cstore.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

// ────────────────────────────────────────────────────────────────
// PRUEBAS DE ACEPTACIÓN — ProductoRest
//
// Requieren:
//   - MySQL corriendo con BD cstore_test
//   - src/test/resources/data.sql con roles, admin, categoria y producto
//   - src/test/resources/application.properties apuntando a cstore_test
// ────────────────────────────────────────────────────────────────
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class ProductoAcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // ── Credenciales del admin insertado en data.sql ──────────────
    private static final String ADMIN_EMAIL    = "admin@cstore.com";
    private static final String ADMIN_PASSWORD = "admin123";

    // Token JWT reutilizado entre tests
    private static String adminToken;

    // Id del producto insertado en data.sql (siempre será 1 con create-drop)
    private static final int PRODUCTO_ID    = 1;
    private static final int CATEGORIA_ID   = 1;

    // ── Helpers ───────────────────────────────────────────────────
    private HttpHeaders headersConToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpHeaders headersPublicos() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ── Obtener token antes de los tests que lo necesitan ─────────
    private String obtenerTokenAdmin() {
        if (adminToken != null) return adminToken;

        Map<String, String> body = Map.of(
            "email",    ADMIN_EMAIL,
            "password", ADMIN_PASSWORD
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/Login",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );
        assertNotNull(response.getBody());
        adminToken = response.getBody()
            .replace("{\"token\":\"", "")
            .replace("\"}", "")
            .trim();
        return adminToken;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 1 — GET productos (sin autenticación)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("GET /Producto/get — sin token → 403 FORBIDDEN")
    void getAllProduct_sinToken_retornaForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/Producto/get", String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(2)
    @DisplayName("GET /Producto/get — autenticado → 200 OK con lista")
    void getAllProduct_autenticado_retornaLista() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/get",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // La lista debe contener al menos el producto del data.sql
        assertTrue(response.getBody().contains("Coca Cola"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 2 — GET por id
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("GET /Producto/getById/{id} — id existente → 200 OK con datos")
    void getProductById_existente_retornaDatos() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/getById/" + PRODUCTO_ID,
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Coca Cola"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /Producto/getById/{id} — id inexistente → 200 OK con wrapper vacío")
    void getProductById_inexistente_retornaVacio() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/getById/9999",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        // El servicio retorna un ProductoWrapper vacío con 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 3 — GET por categoría
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("GET /Producto/getByCategory/{id} — categoría con productos → 200 OK")
    void getByCategory_categoriaConProductos_retornaLista() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/getByCategory/" + CATEGORIA_ID,
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Coca Cola"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /Producto/getByCategory/{id} — categoría sin productos → 200 OK lista vacía")
    void getByCategory_sinProductos_retornaListaVacia() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/getByCategory/9999",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[]", response.getBody().trim());
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 4 — Agregar producto (solo admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("POST /Producto/add — sin token → 403 FORBIDDEN")
    void addProduct_sinToken_retornaForbidden() {
        Map<String, String> body = Map.of(
            "nombre_producto", "Pepsi",
            "descripcion",     "Bebida gaseosa",
            "precio",          "4",
            "stock",           "50",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/Producto/add",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(8)
    @DisplayName("POST /Producto/add — admin + datos completos → 200 OK")
    void addProduct_adminDatosCompletos_retornaOK() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "nombre_producto", "Pepsi",
            "descripcion",     "Bebida gaseosa 500ml",
            "precio",          "4",
            "stock",           "50",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/add",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Producto added Successfully"));
    }

    @Test
    @Order(9)
    @DisplayName("POST /Producto/add — admin + sin nombre → 400 BAD_REQUEST")
    void addProduct_sinNombre_retornaBadRequest() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "descripcion", "Sin nombre",
            "precio",      "4",
            "stock",       "50",
            "IdCategoria", String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/add",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    @DisplayName("POST /Producto/add — admin + sin stock → 400 BAD_REQUEST")
    void addProduct_sinStock_retornaBadRequest() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "nombre_producto", "Fanta",
            "descripcion",     "Bebida",
            "precio",          "4",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
            // falta stock
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/add",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 5 — Actualizar producto (solo admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("POST /Producto/update — sin token → 403 FORBIDDEN")
    void updateProduct_sinToken_retornaForbidden() {
        Map<String, String> body = Map.of(
            "id_producto",     String.valueOf(PRODUCTO_ID),
            "nombre_producto", "Coca Cola Zero",
            "precio",          "5",
            "stock",           "80",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/Producto/update",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(12)
    @DisplayName("POST /Producto/update — admin + producto existe → 200 OK")
    void updateProduct_adminProductoExiste_retornaOK() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "id_producto",     String.valueOf(PRODUCTO_ID),
            "nombre_producto", "Coca Cola Zero",
            "descripcion",     "Sin azúcar",
            "precio",          "5",
            "stock",           "80",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/update",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product Updated Successfully"));
    }

    @Test
    @Order(13)
    @DisplayName("POST /Producto/update — admin + id inexistente → 200 OK con mensaje")
    void updateProduct_idInexistente_retornaMensaje() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "id_producto",     "9999",
            "nombre_producto", "NoExiste",
            "precio",          "1",
            "stock",           "1",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/update",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product id Does not exist"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 6 — Actualizar estado (solo admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(14)
    @DisplayName("POST /Producto/updateStatus — admin + producto existe → 200 OK")
    void updateStatus_adminProductoExiste_retornaOK() {
        String token = obtenerTokenAdmin();

        Map<String, String> body = Map.of(
            "id_producto", String.valueOf(PRODUCTO_ID),
            "status",      "false"
        );

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/updateStatus",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product Status Update Succesfully"));
    }

    @Test
    @Order(15)
    @DisplayName("POST /Producto/updateStatus — sin token → 403 FORBIDDEN")
    void updateStatus_sinToken_retornaForbidden() {
        Map<String, String> body = Map.of(
            "id_producto", String.valueOf(PRODUCTO_ID),
            "status",      "true"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/Producto/updateStatus",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 7 — Eliminar producto (solo admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(16)
    @DisplayName("POST /Producto/delete/{id} — sin token → 403 FORBIDDEN")
    void deleteProduct_sinToken_retornaForbidden() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/Producto/delete/" + PRODUCTO_ID,
            new HttpEntity<>(headersPublicos()),
            String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(17)
    @DisplayName("POST /Producto/delete/{id} — admin + id inexistente → 200 OK con mensaje")
    void deleteProduct_idInexistente_retornaMensaje() {
        String token = obtenerTokenAdmin();

        ResponseEntity<String> response = restTemplate.exchange(
            "/Producto/delete/9999",
            HttpMethod.POST,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Product id does not exist"));
    }

    @Test
    @Order(18)
    @DisplayName("POST /Producto/delete/{id} — admin + id existente → 200 OK eliminado")
    void deleteProduct_adminIdExiste_eliminaYRetornaOK() {
        String token = obtenerTokenAdmin();

        // Primero agregamos un producto nuevo para no borrar el del data.sql
        Map<String, String> bodyAdd = Map.of(
            "nombre_producto", "ParaBorrar",
            "descripcion",     "Temporal",
            "precio",          "1",
            "stock",           "1",
            "IdCategoria",     String.valueOf(CATEGORIA_ID)
        );
        ResponseEntity<String> addResponse = restTemplate.exchange(
            "/Producto/add",
            HttpMethod.POST,
            new HttpEntity<>(bodyAdd, headersConToken(token)),
            String.class
        );
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());

        // Obtenemos la lista para encontrar el id del producto recién creado
        ResponseEntity<String> lista = restTemplate.exchange(
            "/Producto/get",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );
        // El último id es el más alto — lo extraemos buscando "ParaBorrar" en la lista
        // Para simplicidad usamos id=2 (el primero que se agregó en el test 8 fue Pepsi)
        // y el recién creado sería el siguiente disponible
        assertTrue(lista.getBody().contains("ParaBorrar"));

        // Eliminamos — el id exacto depende del orden de creación
        // Buscamos el id en el JSON de forma simple
        String listaJson = lista.getBody();
        int idx = listaJson.lastIndexOf("\"id\":");
        String idStr = listaJson.substring(idx + 5, listaJson.indexOf(",", idx + 5)).trim();
        int idParaBorrar = Integer.parseInt(idStr);

        ResponseEntity<String> deleteResponse = restTemplate.exchange(
            "/Producto/delete/" + idParaBorrar,
            HttpMethod.POST,
            new HttpEntity<>(headersConToken(token)),
            String.class
        );

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertTrue(deleteResponse.getBody().contains("Product Deleted Successfully"));
    }
}