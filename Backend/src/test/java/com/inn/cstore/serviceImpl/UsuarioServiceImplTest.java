package com.inn.cstore.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.inn.cstore.JWT.CustomerUserDetailsService;
import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.JWT.JwtUtil;
import com.inn.cstore.POJO.Rol;
import com.inn.cstore.POJO.Usuario;
import com.inn.cstore.dao.RolDao;
import com.inn.cstore.dao.UsuarioDao;
import com.inn.cstore.servicelmpl.UsuarioServiceImpl;
import com.inn.cstore.utils.EmailUtils;
import com.inn.cstore.wrapper.UsuarioWrapper;

// ────────────────────────────────────────────────────────────────
// PATRÓN: @ExtendWith(MockitoExtension.class) en lugar de
//         @SpringBootTest, porque probamos lógica de negocio pura
//         sin levantar el contexto de Spring.
// ────────────────────────────────────────────────────────────────
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    // ── Dependencias mockeadas ────────────────────────────────────
    @Mock private UsuarioDao usuarioDao;
    @Mock private RolDao rolDao;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomerUserDetailsService customerUserDetailsService;
    @Mock private JwtUtil jwtUtil;
    @Mock private JwtFilter jwtFilter;
    @Mock private EmailUtils emailUtils;

    // ── Clase bajo prueba (SUT) ───────────────────────────────────
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // ── Datos de apoyo ────────────────────────────────────────────
    private Usuario usuarioActivo;
    private Rol rolUsuario;

    @BeforeEach
    void setUp() {
        rolUsuario = new Rol();
        rolUsuario.setId(2);
        rolUsuario.setNombre("usuario");

        usuarioActivo = new Usuario();
        usuarioActivo.setId(1);
        usuarioActivo.setNombre("Diego Torres");
        usuarioActivo.setEmail("diego@test.com");
        usuarioActivo.setPassword("pass123");
        usuarioActivo.setEstado("true");
        usuarioActivo.setRol(rolUsuario);
    }

    // ═══════════════════════════════════════════════════════════════
    //  singUp
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("singUp: campos requeridos faltantes → BAD_REQUEST")
    void singUp_camposFaltantes_retornaBadRequest() {
        // Arrange: mapa sin "nombre" (campo obligatorio)
        Map<String, String> request = Map.of(
            "email", "test@mail.com",
            "contraseña", "123456"
            // falta "nombre" y "numerocontacto"
        );

        // Act
        ResponseEntity<String> response = usuarioService.singUp(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verificamos que el DAO nunca fue llamado
        verify(usuarioDao, never()).save(any());
    }

    @Test
    @DisplayName("singUp: email ya registrado → BAD_REQUEST con mensaje de duplicado")
    void singUp_emailDuplicado_retornaBadRequest() {
        // Arrange: simulamos que el email YA existe en la BD
        Map<String, String> request = Map.of(
            "nombre", "Diego",
            "numerocontacto", "987654321",
            "email", "diego@test.com",
            "contraseña", "pass123"
        );
        when(usuarioDao.findByEmail("diego@test.com")).thenReturn(usuarioActivo);

        // Act
        ResponseEntity<String> response = usuarioService.singUp(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email ya creado"));
        verify(usuarioDao, never()).save(any());
    }

    @Test
    @DisplayName("singUp: datos válidos y email nuevo → OK + usuario guardado")
    void singUp_datosValidos_guardaUsuarioYRetornaOK() {
        // Arrange
        Map<String, String> request = Map.of(
            "nombre", "Nuevo Usuario",
            "numerocontacto", "999888777",
            "email", "nuevo@mail.com",
            "contraseña", "segura123"
        );
        // El email NO existe aún
        when(usuarioDao.findByEmail("nuevo@mail.com")).thenReturn(null);
        // El rol "usuario" existe en BD
        when(rolDao.findByNombre("usuario")).thenReturn(Optional.of(rolUsuario));

        // Act
        ResponseEntity<String> response = usuarioService.singUp(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Succesfully Registered"));
        // El DAO save fue llamado exactamente una vez
        verify(usuarioDao, times(1)).save(any(Usuario.class));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Login
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Login: credenciales válidas + cuenta activa → OK con token JWT")
    void login_credencialesValidas_retornaToken() {
        // Arrange
        Map<String, String> request = Map.of(
            "email", "diego@test.com",
            "password", "pass123"
        );
        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authMock);
        when(customerUserDetailsService.getUserDetail()).thenReturn(usuarioActivo);
        when(jwtUtil.generateToken("diego@test.com", "usuario")).thenReturn("mocked.jwt.token");

        // Act
        ResponseEntity<String> response = usuarioService.Login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("mocked.jwt.token"));
        verify(jwtUtil, times(1)).generateToken("diego@test.com", "usuario");
    }

    @Test
    @DisplayName("Login: cuenta inactiva (estado=false) → BAD_REQUEST + mensaje de espera")
    void login_cuentaInactiva_retornaMensajeEspera() {
        // Arrange
        usuarioActivo.setEstado("false");  // cuenta pendiente de aprobación
        Map<String, String> request = Map.of(
            "email", "diego@test.com",
            "password", "pass123"
        );
        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authMock);
        when(customerUserDetailsService.getUserDetail()).thenReturn(usuarioActivo);

        // Act
        ResponseEntity<String> response = usuarioService.Login(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Wait for admin approval."));
        // El token NUNCA debe generarse para cuentas inactivas
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login: excepción en autenticación → BAD_REQUEST + mensaje Bad Credentials")
    void login_excepcionAutenticacion_retornaBadCredentials() {
        // Arrange
        Map<String, String> request = Map.of("email", "x@x.com", "password", "wrong");
        when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Bad credentials"));

        // Act
        ResponseEntity<String> response = usuarioService.Login(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Bad Credentials."));
    }

    // ═══════════════════════════════════════════════════════════════
    //  changePassword
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("changePassword: contraseña antigua correcta → OK + contraseña actualizada")
    void changePassword_passwordCorrecta_actualizaYRetornaOK() {
        // Arrange
        Map<String, String> request = Map.of(
            "oldPassword", "pass123",
            "newPassword", "nueva456"
        );
        when(jwtFilter.getCurrentUserName()).thenReturn("diego@test.com");
        when(usuarioDao.findByEmail("diego@test.com")).thenReturn(usuarioActivo);

        // Act
        ResponseEntity<String> response = usuarioService.changePassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("nueva456", usuarioActivo.getPassword());
        verify(usuarioDao, times(1)).save(usuarioActivo);
    }

    @Test
    @DisplayName("changePassword: contraseña antigua incorrecta → BAD_REQUEST")
    void changePassword_passwordIncorrecta_retornaBadRequest() {
        // Arrange
        Map<String, String> request = Map.of(
            "oldPassword", "incorrecta",
            "newPassword", "nueva456"
        );
        when(jwtFilter.getCurrentUserName()).thenReturn("diego@test.com");
        when(usuarioDao.findByEmail("diego@test.com")).thenReturn(usuarioActivo);

        // Act
        ResponseEntity<String> response = usuarioService.changePassword(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Contraseña antigua incorrecta"));
        // La contraseña no cambió
        assertEquals("pass123", usuarioActivo.getPassword());
        verify(usuarioDao, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════
    //  getAllUsuarios
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllUsuarios: rol admin → OK con lista completa")
    void getAllUsuarios_esAdmin_retornaListaOK() {
        // Arrange
        List<UsuarioWrapper> listaEsperada = List.of(new UsuarioWrapper());
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(usuarioDao.getAllUsuarios()).thenReturn(listaEsperada);

        // Act
        ResponseEntity<List<UsuarioWrapper>> response = usuarioService.getAllUsuarios();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("getAllUsuarios: no es admin → UNAUTHORIZED con lista vacía")
    void getAllUsuarios_noEsAdmin_retornaUnauthorized() {
        // Arrange
        when(jwtFilter.isAdmin()).thenReturn(false);

        // Act
        ResponseEntity<List<UsuarioWrapper>> response = usuarioService.getAllUsuarios();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(usuarioDao, never()).getAllUsuarios();
    }
// ═══════════════════════════════════════════════════════════════
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateRol: no es admin → UNAUTHORIZED")
    void updateRol_noEsAdmin_retornaUnauthorized() {
        // Arrange
        when(jwtFilter.isAdmin()).thenReturn(false);

        // Act
        ResponseEntity<String> response = usuarioService.updateRol(Map.of("id", "1", "rol", "admin"));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(rolDao, never()).findByNombre(anyString());
    }

    @Test
    @DisplayName("updateRol: usuario no existe → NOT_FOUND")
    void updateRol_usuarioNoExiste_retornaNotFound() {
        // Arrange
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(rolDao.findByNombre("admin")).thenReturn(Optional.of(new Rol()));
        when(usuarioDao.findById(99)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = usuarioService.updateRol(Map.of("id", "99", "rol", "admin"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}