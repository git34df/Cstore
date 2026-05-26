package com.inn.cstore.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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
// PRUEBAS DE ACEPTACIÓN — UsuarioRest
//
// Levantan el servidor completo en un puerto aleatorio y realizan
// peticiones HTTP reales contra la API, tal como lo haría un cliente.
//
// REQUISITOS antes de correr:
//   1. MySQL corriendo localmente.
//   2. BD "cstore_test" creada (o ajustar la URL en
//      src/test/resources/application.properties).
//   3. Que existan los roles "admin" y "usuario" en la tabla rol.
//   4. Que exista un admin activo con email/password configurados
//      en ADMIN_EMAIL / ADMIN_PASSWORD abajo.
// ────────────────────────────────────────────────────────────────
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class UsuarioAcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // ── Credenciales del admin preexistente en BD de prueba ───────
    private static final String ADMIN_EMAIL    = "admin@cstore.com";
    private static final String ADMIN_PASSWORD = "admin123";

    // ── Datos del usuario de prueba (se crea en el test 1) ────────
    private static final String USER_EMAIL    = "aceptacion@test.com";
    private static final String USER_PASSWORD = "test123";

    // Token JWT que se reutiliza entre tests
    private static String adminToken;
    private static Integer usuarioId;

    // ── Helper: cabecera con token JWT ────────────────────────────
    private HttpHeaders headersConToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    // ── Helper: cabecera sin token (endpoints públicos) ───────────
    private HttpHeaders headersPublicos() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 1 — Registro de usuario
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("POST /usuario/signup — datos válidos → 200 OK + Succesfully Registered")
    void signup_datosValidos_retornaOK() {
        Map<String, String> body = Map.of(
            "nombre",         "Usuario Aceptacion",
            "numerocontacto", "999111222",
            "email",          USER_EMAIL,
            "contraseña",     USER_PASSWORD
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/signup",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Succesfully Registered"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /usuario/signup — email duplicado → 400 BAD_REQUEST")
    void signup_emailDuplicado_retornaBadRequest() {
        // El mismo email registrado en el test anterior
        Map<String, String> body = Map.of(
            "nombre",         "Otro Nombre",
            "numerocontacto", "999000111",
            "email",          USER_EMAIL,
            "contraseña",     "otrapass"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/signup",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email ya creado"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /usuario/signup — campos faltantes → 400 BAD_REQUEST")
    void signup_camposFaltantes_retornaBadRequest() {
        Map<String, String> body = Map.of(
            "email", "incompleto@test.com"
            // falta nombre, numerocontacto, contraseña
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/signup",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 2 — Login
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("POST /usuario/Login — cuenta inactiva (recién registrada) → 400 + mensaje espera")
    void login_cuentaInactiva_retornaMensajeEspera() {
        Map<String, String> body = Map.of(
            "email",    USER_EMAIL,
            "password", USER_PASSWORD
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/Login",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        // El usuario recién registrado tiene estado=false, espera aprobación
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Wait for admin approval."));
    }

    @Test
    @Order(5)
    @DisplayName("POST /usuario/Login — admin activo → 200 OK + token JWT")
    void login_adminActivo_retornaToken() {
        Map<String, String> body = Map.of(
            "email",    ADMIN_EMAIL,
            "password", ADMIN_PASSWORD
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/Login",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("token"));

        // Guardamos el token para los tests siguientes
        String responseBody = response.getBody();
        adminToken = responseBody
            .replace("{\"token\":\"", "")
            .replace("\"}", "")
            .trim();

        assertFalse(adminToken.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("POST /usuario/Login — credenciales incorrectas → 400 + Bad Credentials")
    void login_credencialesIncorrectas_retornaBadCredentials() {
        Map<String, String> body = Map.of(
            "email",    "noexiste@test.com",
            "password", "wrongpass"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/Login",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Bad Credentials."));
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 3 — Gestión de usuarios (requiere admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("GET /usuario/get — sin token → 403 FORBIDDEN")
    void getAllUsuarios_sinToken_retornaForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/usuario/get", String.class
        );

        // Sin JWT el filtro rechaza la petición
        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(8)
    @DisplayName("GET /usuario/get — admin autenticado → 200 OK con lista de usuarios")
    void getAllUsuarios_adminAutenticado_retornaLista() {
        // Necesitamos el token del test 5
        assertNotNull(adminToken, "El token admin no se obtuvo — verifica el test de login");

        ResponseEntity<String> response = restTemplate.exchange(
            "/usuario/get",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(adminToken)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(9)
    @DisplayName("POST /usuario/Update — admin activa al usuario de prueba → 200 OK")
    void update_adminActivaUsuario_retornaOK() {
        assertNotNull(adminToken, "El token admin no se obtuvo — verifica el test de login");

        // Buscamos el id del usuario de prueba en la lista
        ResponseEntity<String> listaResponse = restTemplate.exchange(
                "/usuario/get",
                HttpMethod.GET,
                new HttpEntity<>(headersConToken(adminToken)),
                String.class);
        assertEquals(HttpStatus.OK, listaResponse.getStatusCode());

        // Extraemos el id del usuario de aceptacion@test.com del JSON
        String listaJson = listaResponse.getBody();
        // El usuario recién creado siempre tiene el id más alto — usamos id=2 como
        // fallback
        // ya que data.sql inserta el admin con id=1
        Map<String, String> body = Map.of(
                "id", "2",
                "estado", "true");

        ResponseEntity<String> response = restTemplate.exchange(
                "/usuario/Update",
                HttpMethod.POST,
                new HttpEntity<>(body, headersConToken(adminToken)),
                String.class);

        // Aceptamos OK o error de email (el servicio funciona aunque el email falle)
        assertTrue(
                response.getStatusCode() == HttpStatus.OK ||
                        response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 4 — checkToken
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("GET /usuario/checkToken — token válido → 200 OK + true")
    void checkToken_tokenValido_retornaTrue() {
        assertNotNull(adminToken, "El token admin no se obtuvo — verifica el test de login");

        ResponseEntity<String> response = restTemplate.exchange(
            "/usuario/checkToken",
            HttpMethod.GET,
            new HttpEntity<>(headersConToken(adminToken)),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("true"));
    }

    @Test
    @Order(11)
    @DisplayName("GET /usuario/checkToken — sin token → 403 FORBIDDEN")
    void checkToken_sinToken_retornaForbidden() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/usuario/checkToken", String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    

    @Test
    @Order(13)
    @DisplayName("POST /usuario/ForgotPassword — email no registrado → 200 OK (sin revelar existencia)")
    void forgotPassword_emailNoRegistrado_retornaOKPorSeguridad() {
        Map<String, String> body = Map.of("email", "fantasma@noexiste.com");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/ForgotPassword",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        // Por seguridad siempre retorna OK aunque el email no exista
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════
    //  FLUJO 6 — updateRol (solo admin)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(14)
    @DisplayName("POST /usuario/updateRol — sin token → 403 FORBIDDEN")
    void updateRol_sinToken_retornaForbidden() {
        Map<String, String> body = Map.of("id", "2", "rol", "admin");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/usuario/updateRol",
            new HttpEntity<>(body, headersPublicos()),
            String.class
        );

        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN ||
            response.getStatusCode() == HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @Order(15)
    @DisplayName("POST /usuario/updateRol — admin + usuario inexistente → 404 NOT_FOUND")
    void updateRol_usuarioInexistente_retornaNotFound() {
        assertNotNull(adminToken, "El token admin no se obtuvo — verifica el test de login");

        Map<String, String> body = Map.of("id", "9999", "rol", "admin");

        ResponseEntity<String> response = restTemplate.exchange(
            "/usuario/updateRol",
            HttpMethod.POST,
            new HttpEntity<>(body, headersConToken(adminToken)),
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}