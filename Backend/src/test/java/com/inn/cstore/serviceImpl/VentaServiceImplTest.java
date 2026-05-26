package com.inn.cstore.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import com.inn.cstore.POJO.Comprobante;
import com.inn.cstore.POJO.Pago;
import com.inn.cstore.POJO.Producto;
import com.inn.cstore.POJO.Rol;
import com.inn.cstore.POJO.Usuario;
import com.inn.cstore.POJO.Venta;
import com.inn.cstore.POJO.VentaDetalle;
import com.inn.cstore.dao.ComprobanteDao;
import com.inn.cstore.dao.PagoDao;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.dao.UsuarioDao;
import com.inn.cstore.dao.VentaDao;
import com.inn.cstore.dao.VentaDetalleDao;
import com.inn.cstore.servicelmpl.VentaServiceImpl;
import com.inn.cstore.wrapper.VentaWrapper;

// ────────────────────────────────────────────────────────────────
// NOTA SOBRE registrarVenta (happy path):
// El método genera un PDF con iTextPDF en STORE_LOCATION.
// En tests unitarios con Mockito puro no podemos interceptar el
// FileOutputStream interno. Los tests del happy path completo
// pertenecen a Integration Tests con @SpringBootTest + @TempDir.
// Aquí cubrimos todos los paths de error (que son los más críticos).
// ────────────────────────────────────────────────────────────────
@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock private JwtFilter jwtFilter;
    @Mock private VentaDao ventaDao;
    @Mock private VentaDetalleDao ventaDetalleDao;
    @Mock private ComprobanteDao comprobanteDao;
    @Mock private PagoDao pagoDao;
    @Mock private ProductoDao productoDao;
    @Mock private UsuarioDao usuarioDao;

    @InjectMocks
    private VentaServiceImpl ventaService;

    // ── Datos de apoyo ────────────────────────────────────────────
    private Usuario usuario;
    private Producto producto;
    private Venta ventaCompletada;
    private Venta ventaAnulada;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setNombre("usuario");

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail("cajero@test.com");
        usuario.setRol(rol);

        Categoria cat = new Categoria();
        cat.setId(1);
        cat.setNombre("Electrónica");

        producto = new Producto();
        producto.setId(10);
        producto.setNombre("Audífonos");
        producto.setPrice(100);
        producto.setStock(15);
        producto.setCategoria(cat);

        ventaCompletada = new Venta();
        ventaCompletada.setId(1);
        ventaCompletada.setFecha(LocalDateTime.now());
        ventaCompletada.setTotal(200);
        ventaCompletada.setEstado("COMPLETADA");
        ventaCompletada.setUsuario(usuario);

        ventaAnulada = new Venta();
        ventaAnulada.setId(2);
        ventaAnulada.setEstado("ANULADA");
        ventaAnulada.setUsuario(usuario);
    }

    // ─── Helper: request de venta mínimo válido ──────────────────
    private Map<String, Object> requestVentaValido() {
        Map<String, Object> item = Map.of(
            "productoId", "10",
            "cantidad", "2",
            "precioUnitario", "100"
        );
        return Map.of(
            "detalle", List.of(item),
            "total", "200",
            "nombreCliente", "Juan Pérez",
            "emailCliente", "juan@test.com",
            "telefonoCliente", "987654321",
            "metodoPago", "EFECTIVO",
            "tipoComprobante", "BOLETA"
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  registrarVenta — paths de error (testables sin PDF)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registrarVenta: falta campo 'total' → BAD_REQUEST")
    void registrarVenta_faltaTotal_retornaBadRequest() {
        // Arrange: request sin "total"
        Map<String, Object> request = Map.of(
            "detalle", List.of(),
            "nombreCliente", "Juan",
            "emailCliente", "juan@test.com",
            "telefonoCliente", "999",
            "metodoPago", "EFECTIVO"
            // falta "total"
        );

        // Act
        ResponseEntity<String> response = ventaService.registrarVenta(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Datos requeridos no encontrados"));
        verify(ventaDao, never()).save(any());
    }

    @Test
    @DisplayName("registrarVenta: falta campo 'detalle' → BAD_REQUEST")
    void registrarVenta_faltaDetalle_retornaBadRequest() {
        Map<String, Object> request = Map.of(
            "total", "100",
            "nombreCliente", "Juan",
            "emailCliente", "j@j.com",
            "telefonoCliente", "999",
            "metodoPago", "EFECTIVO"
        );

        ResponseEntity<String> response = ventaService.registrarVenta(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(ventaDao, never()).save(any());
    }

    @Test
    @DisplayName("registrarVenta: usuario autenticado no encontrado en BD → UNAUTHORIZED")
    void registrarVenta_usuarioNoExiste_retornaUnauthorized() {
        // Arrange: el JWT dice que el usuario es X pero no existe en BD
        when(jwtFilter.getCurrentUserName()).thenReturn("fantasma@test.com");
        when(usuarioDao.findByEmail("fantasma@test.com")).thenReturn(null);

        ResponseEntity<String> response = ventaService.registrarVenta(requestVentaValido());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario no encontrado"));
        verify(ventaDao, never()).save(any());
    }

    @Test
    @DisplayName("registrarVenta: stock insuficiente → BAD_REQUEST con nombre del producto")
    void registrarVenta_stockInsuficiente_retornaBadRequestConNombreProducto() {
        // Arrange: producto con stock=1 pero se piden 5 unidades
        producto.setStock(1);
        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(99);
        ventaGuardada.setUsuario(usuario);

        when(jwtFilter.getCurrentUserName()).thenReturn("cajero@test.com");
        when(usuarioDao.findByEmail("cajero@test.com")).thenReturn(usuario);
        when(ventaDao.save(any(Venta.class))).thenReturn(ventaGuardada);

        Map<String, Object> itemConDemasidaCantidad = Map.of(
            "productoId", "10",
            "cantidad", "5",       // pide 5, hay 1
            "precioUnitario", "100"
        );
        Map<String, Object> request = Map.of(
            "detalle", List.of(itemConDemasidaCantidad),
            "total", "500",
            "nombreCliente", "Juan",
            "emailCliente", "j@j.com",
            "telefonoCliente", "999",
            "metodoPago", "EFECTIVO"
        );

        when(productoDao.findById(10)).thenReturn(Optional.of(producto));

        // Act
        ResponseEntity<String> response = ventaService.registrarVenta(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Stock insuficiente para: Audífonos"));
        // El stock NO debe haberse modificado
        assertEquals(1, producto.getStock());
    }

    @Test
    @DisplayName("registrarVenta: producto del detalle no existe en BD → BAD_REQUEST")
    void registrarVenta_productoDelDetalleNoExiste_retornaBadRequest() {
        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(99);
        ventaGuardada.setUsuario(usuario);

        when(jwtFilter.getCurrentUserName()).thenReturn("cajero@test.com");
        when(usuarioDao.findByEmail("cajero@test.com")).thenReturn(usuario);
        when(ventaDao.save(any(Venta.class))).thenReturn(ventaGuardada);
        // El producto 999 no existe
        when(productoDao.findById(999)).thenReturn(Optional.empty());

        Map<String, Object> item = Map.of(
            "productoId", "999",
            "cantidad", "1",
            "precioUnitario", "50"
        );
        Map<String, Object> request = Map.of(
            "detalle", List.of(item),
            "total", "50",
            "nombreCliente", "Ana",
            "emailCliente", "ana@test.com",
            "telefonoCliente", "111",
            "metodoPago", "TARJETA"
        );

        ResponseEntity<String> response = ventaService.registrarVenta(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Producto no encontrado"));
    }

    // ═══════════════════════════════════════════════════════════════
    //  getVentas
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getVentas: admin → retorna todas las ventas (getAllVentas)")
    void getVentas_esAdmin_retornaTodasLasVentas() {
        when(jwtFilter.isAdmin()).thenReturn(true);
        when(ventaDao.getAllVentas()).thenReturn(List.of(ventaCompletada));
        // Para buildWrapper necesitamos mocks de comprobante, pago y detalle
        when(comprobanteDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(pagoDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(ventaDetalleDao.getDetalleByVenta(1)).thenReturn(List.of());

        ResponseEntity<List<VentaWrapper>> response = ventaService.getVentas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).getId());
        // Verificamos que se usó el método de admin (no el de usuario)
        verify(ventaDao, times(1)).getAllVentas();
        verify(ventaDao, never()).getVentasByUsuario(anyString());
    }

    @Test
    @DisplayName("getVentas: no es admin → retorna solo las ventas del usuario logueado")
    void getVentas_noEsAdmin_retornaVentasDelUsuario() {
        when(jwtFilter.isAdmin()).thenReturn(false);
        when(jwtFilter.getCurrentUserName()).thenReturn("cajero@test.com");
        when(ventaDao.getVentasByUsuario("cajero@test.com")).thenReturn(List.of(ventaCompletada));
        when(comprobanteDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(pagoDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(ventaDetalleDao.getDetalleByVenta(1)).thenReturn(List.of());

        ResponseEntity<List<VentaWrapper>> response = ventaService.getVentas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(ventaDao, never()).getAllVentas();
        verify(ventaDao, times(1)).getVentasByUsuario("cajero@test.com");
    }

    // ═══════════════════════════════════════════════════════════════
    //  getVentaById
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getVentaById: id existente → OK con wrapper completo")
    void getVentaById_idExistente_retornaWrapperOK() {
        when(ventaDao.findById(1)).thenReturn(Optional.of(ventaCompletada));
        when(comprobanteDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(pagoDao.findByVentaId(1)).thenReturn(Optional.empty());
        when(ventaDetalleDao.getDetalleByVenta(1)).thenReturn(List.of());

        ResponseEntity<VentaWrapper> response = ventaService.getVentaById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("COMPLETADA", response.getBody().getEstado());
        assertEquals(200, response.getBody().getTotal());
    }

    @Test
    @DisplayName("getVentaById: id inexistente → NOT_FOUND")
    void getVentaById_idInexistente_retornaNotFound() {
        when(ventaDao.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<VentaWrapper> response = ventaService.getVentaById(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════
    //  anularVenta
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("anularVenta: id inexistente → NOT_FOUND")
    void anularVenta_idInexistente_retornaNotFound() {
        when(ventaDao.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<String> response = ventaService.anularVenta(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Venta no encontrada"));
        verify(ventaDao, never()).save(any());
    }

    @Test
    @DisplayName("anularVenta: venta ya anulada → BAD_REQUEST, sin doble anulación")
    void anularVenta_yaAnulada_retornaBadRequest() {
        when(ventaDao.findById(2)).thenReturn(Optional.of(ventaAnulada));

        ResponseEntity<String> response = ventaService.anularVenta(2);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("La venta ya está anulada"));
        // No debe guardar ni modificar estado
        verify(ventaDao, never()).save(any());
    }

    @Test
    @DisplayName("anularVenta: venta completada → OK + estado=ANULADA + pago=RECHAZADO")
    void anularVenta_ventaCompletada_anulaYActualizaPago() {
        Pago pago = new Pago();
        pago.setEstado("COMPLETADO");
        pago.setVenta(ventaCompletada);

        when(ventaDao.findById(1)).thenReturn(Optional.of(ventaCompletada));
        when(pagoDao.findByVentaId(1)).thenReturn(Optional.of(pago));

        ResponseEntity<String> response = ventaService.anularVenta(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Venta anulada correctamente"));

        // Verificamos que el estado de la venta cambió
        assertEquals("ANULADA", ventaCompletada.getEstado());
        verify(ventaDao, times(1)).save(ventaCompletada);

        // Verificamos que el pago también se rechazó
        assertEquals("RECHAZADO", pago.getEstado());
        verify(pagoDao, times(1)).save(pago);
    }

    @Test
    @DisplayName("anularVenta: venta sin pago asociado → OK + estado=ANULADA (sin NPE)")
    void anularVenta_sinPagoAsociado_anulaCorrectamenteSinError() {
        when(ventaDao.findById(1)).thenReturn(Optional.of(ventaCompletada));
        when(pagoDao.findByVentaId(1)).thenReturn(Optional.empty());

        ResponseEntity<String> response = ventaService.anularVenta(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ANULADA", ventaCompletada.getEstado());
        // El save del pago nunca es llamado (no hay pago)
        verify(pagoDao, never()).save(any());
    }
}