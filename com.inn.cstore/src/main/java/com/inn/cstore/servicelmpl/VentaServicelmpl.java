package com.inn.cstore.servicelmpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Comprobante;
import com.inn.cstore.POJO.Pago;
import com.inn.cstore.POJO.Producto;
import com.inn.cstore.POJO.Usuario;
import com.inn.cstore.POJO.Venta;
import com.inn.cstore.POJO.VentaDetalle;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.ComprobanteDao;
import com.inn.cstore.dao.PagoDao;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.dao.UsuarioDao;
import com.inn.cstore.dao.VentaDao;
import com.inn.cstore.dao.VentaDetalleDao;
import com.inn.cstore.service.VentaService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.VentaDetalleWrapper;
import com.inn.cstore.wrapper.VentaWrapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VentaServicelmpl implements VentaService {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private VentaDao ventaDao;

    @Autowired
    private VentaDetalleDao ventaDetalleDao;

    @Autowired
    private ComprobanteDao comprobanteDao;

    @Autowired
    private PagoDao pagoDao;

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private UsuarioDao usuarioDao;

    // ─────────────────────────────────────────────
    // REGISTRAR VENTA COMPLETA
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public ResponseEntity<String> registrarVenta(Map<String, Object> requestMap) {
        log.info("Inside registrarVenta");
        try {
            if (!validarRequest(requestMap)) {
                return CstoreUtils.getResponseEntity("Datos requeridos no encontrados", HttpStatus.BAD_REQUEST);
            }

            // 1. Obtener usuario autenticado
            String emailUsuario = jwtFilter.getCurrentUserName();
            Usuario usuario = usuarioDao.findByEmail(emailUsuario);
            if (usuario == null) {
                return CstoreUtils.getResponseEntity("Usuario no encontrado", HttpStatus.UNAUTHORIZED);
            }

            // 2. Construir y guardar la Venta
            Venta venta = new Venta();
            venta.setFecha(LocalDateTime.now());
            venta.setTotal(Integer.parseInt(requestMap.get("total").toString()));
            venta.setEstado("COMPLETADA");
            venta.setUsuario(usuario);
            venta = ventaDao.save(venta);

            // 3. Procesar detalle y descontar stock
            List<Map<String, Object>> items = (List<Map<String, Object>>) requestMap.get("detalle");
            for (Map<String, Object> item : items) {
                Integer productoId = Integer.parseInt(item.get("productoId").toString());
                Integer cantidad = Integer.parseInt(item.get("cantidad").toString());
                Integer precioUnitario = Integer.parseInt(item.get("precioUnitario").toString());

                Producto producto = productoDao.findById(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

                if (producto.getStock() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
                }

                // Descontar stock
                producto.setStock(producto.getStock() - cantidad);
                productoDao.save(producto);

                // Guardar línea de detalle
                VentaDetalle detalle = new VentaDetalle();
                detalle.setVenta(venta);
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setSubtotal(precioUnitario * cantidad);
                ventaDetalleDao.save(detalle);
            }

            // 4. Guardar Comprobante
            String uuid = CstoreUtils.getUUID();
            Comprobante comprobante = new Comprobante();
            comprobante.setVenta(venta);
            comprobante.setUuid(uuid);
            comprobante.setTipo((String) requestMap.getOrDefault("tipoComprobante", "BOLETA"));
            comprobante.setNombreCliente((String) requestMap.get("nombreCliente"));
            comprobante.setEmailCliente((String) requestMap.get("emailCliente"));
            comprobante.setTelefonoCliente((String) requestMap.get("telefonoCliente"));
            comprobanteDao.save(comprobante);

            // 5. Guardar Pago
            Pago pago = new Pago();
            pago.setVenta(venta);
            pago.setMetodo((String) requestMap.getOrDefault("metodoPago", "EFECTIVO"));
            pago.setMonto(venta.getTotal());
            pago.setFechaPago(LocalDateTime.now());
            pago.setEstado("COMPLETADO");
            pagoDao.save(pago);

            // 6. Generar PDF
            generarPdf(uuid, requestMap, venta, items);

            return new ResponseEntity<>("{\"uuid\":\"" + uuid + "\"}", HttpStatus.OK);

        } catch (RuntimeException e) {
            log.error("Error en registrarVenta: {}", e.getMessage());
            return CstoreUtils.getResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────
    // OBTENER VENTAS
    // ─────────────────────────────────────────────
    @Override
    public ResponseEntity<List<VentaWrapper>> getVentas() {
        try {
            List<Venta> ventas;
            if (jwtFilter.isAdmin()) {
                ventas = ventaDao.getAllVentas();
            } else {
                ventas = ventaDao.getVentasByUsuario(jwtFilter.getCurrentUserName());
            }
            return new ResponseEntity<>(mapVentasToWrappers(ventas), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────
    // OBTENER VENTA POR ID (con detalle completo)
    // ─────────────────────────────────────────────
    @Override
    public ResponseEntity<VentaWrapper> getVentaById(Integer id) {
        try {
            Optional<Venta> optional = ventaDao.findById(id);
            if (optional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Venta venta = optional.get();
            VentaWrapper wrapper = buildWrapper(venta);
            return new ResponseEntity<>(wrapper, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────
    // OBTENER / REGENERAR PDF
    // ─────────────────────────────────────────────
    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        try {
            String uuid = (String) requestMap.get("uuid");
            if (uuid == null || uuid.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            String filePath = CstoreConstants.STORE_LOCATION + "\\" + uuid + ".pdf";

            if (!CstoreUtils.isFileExist(filePath)) {
                // Recuperar datos desde BD para regenerar
                Comprobante comp = comprobanteDao.findByUuid(uuid)
                        .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));
                Venta venta = comp.getVenta();
                List<Map<String, Object>> items = buildItemsFromDetalle(venta);
                generarPdf(uuid, buildRequestFromComprobante(comp, venta), venta, items);
            }

            byte[] byteArray = getByteArray(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline").filename(uuid + ".pdf").build());
            return new ResponseEntity<>(byteArray, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────
    // ANULAR VENTA
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public ResponseEntity<String> anularVenta(Integer id) {
        try {
            Optional<Venta> optional = ventaDao.findById(id);
            if (optional.isEmpty()) {
                return CstoreUtils.getResponseEntity("Venta no encontrada", HttpStatus.NOT_FOUND);
            }
            Venta venta = optional.get();
            if ("ANULADA".equals(venta.getEstado())) {
                return CstoreUtils.getResponseEntity("La venta ya está anulada", HttpStatus.BAD_REQUEST);
            }
            venta.setEstado("ANULADA");
            ventaDao.save(venta);

            // Actualizar estado del pago asociado
            pagoDao.findByVentaId(id).ifPresent(pago -> {
                pago.setEstado("RECHAZADO");
                pagoDao.save(pago);
            });

            return CstoreUtils.getResponseEntity("Venta anulada correctamente", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────
    // MÉTODOS PRIVADOS
    // ─────────────────────────────────────────────

    private boolean validarRequest(Map<String, Object> requestMap) {
        return requestMap.containsKey("detalle") &&
                requestMap.containsKey("total") &&
                requestMap.containsKey("nombreCliente") &&
                requestMap.containsKey("emailCliente") &&
                requestMap.containsKey("telefonoCliente") &&
                requestMap.containsKey("metodoPago");
    }

    private List<VentaWrapper> mapVentasToWrappers(List<Venta> ventas) {
        List<VentaWrapper> result = new ArrayList<>();
        for (Venta v : ventas) {
            result.add(buildWrapper(v));
        }
        return result;
    }

    private VentaWrapper buildWrapper(Venta venta) {
        VentaWrapper w = new VentaWrapper();
        w.setId(venta.getId());
        w.setFecha(venta.getFecha());
        w.setTotal(venta.getTotal());
        w.setEstado(venta.getEstado());
        w.setUsuarioEmail(venta.getUsuario().getEmail());

        // Comprobante
        comprobanteDao.findByVentaId(venta.getId()).ifPresent(c -> {
            w.setComprobanteUuid(c.getUuid());
            w.setComprobanteTipo(c.getTipo());
            w.setNombreCliente(c.getNombreCliente());
            w.setEmailCliente(c.getEmailCliente());
            w.setTelefonoCliente(c.getTelefonoCliente());
        });

        // Pago
        pagoDao.findByVentaId(venta.getId()).ifPresent(p -> {
            w.setPagoMetodo(p.getMetodo());
            w.setPagoEstado(p.getEstado());
        });

        // Detalle
        List<VentaDetalle> detalles = ventaDetalleDao.getDetalleByVenta(venta.getId());
        List<VentaDetalleWrapper> detalleWrappers = new ArrayList<>();
        for (VentaDetalle vd : detalles) {
            detalleWrappers.add(new VentaDetalleWrapper(
                    vd.getProducto().getId(),
                    vd.getProducto().getNombre(),
                    vd.getProducto().getCategoria().getNombre(),
                    vd.getCantidad(),
                    vd.getPrecioUnitario(),
                    vd.getSubtotal()
            ));
        }
        w.setDetalle(detalleWrappers);
        return w;
    }

    private void generarPdf(String uuid, Map<String, Object> requestMap, Venta venta,
                             List<Map<String, Object>> items) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document,
                new FileOutputStream(CstoreConstants.STORE_LOCATION + "\\" + uuid + ".pdf"));
        document.open();
        setRectangleInPdf(document);

        Paragraph titulo = new Paragraph("Cstore - Comprobante de Venta", getFont("Header"));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        String tipo = (String) requestMap.getOrDefault("tipoComprobante", "BOLETA");
        String info = "Tipo: " + tipo + "\n" +
                "Cliente: " + requestMap.get("nombreCliente") + "\n" +
                "Teléfono: " + requestMap.get("telefonoCliente") + "\n" +
                "Email: " + requestMap.get("emailCliente") + "\n" +
                "Método de pago: " + requestMap.get("metodoPago") + "\n\n";
        document.add(new Paragraph(info, getFont("Data")));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        addTableHeader(table);

        for (Map<String, Object> item : items) {
            table.addCell(String.valueOf(item.get("productoNombre")));
            table.addCell(String.valueOf(item.get("categoriaNombre")));
            table.addCell(String.valueOf(item.get("cantidad")));
            table.addCell(String.valueOf(item.get("precioUnitario")));
            table.addCell(String.valueOf(item.get("subtotal")));
        }
        document.add(table);

        Paragraph footer = new Paragraph(
                "\nTotal: S/ " + venta.getTotal() + "\nGracias por su compra.", getFont("Data"));
        document.add(footer);
        document.close();
    }

    private List<Map<String, Object>> buildItemsFromDetalle(Venta venta) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (VentaDetalle vd : ventaDetalleDao.getDetalleByVenta(venta.getId())) {
            items.add(Map.of(
                    "productoNombre", vd.getProducto().getNombre(),
                    "categoriaNombre", vd.getProducto().getCategoria().getNombre(),
                    "cantidad", vd.getCantidad(),
                    "precioUnitario", vd.getPrecioUnitario(),
                    "subtotal", vd.getSubtotal()
            ));
        }
        return items;
    }

    private Map<String, Object> buildRequestFromComprobante(Comprobante c, Venta v) {
        return Map.of(
                "tipoComprobante", c.getTipo(),
                "nombreCliente", c.getNombreCliente() != null ? c.getNombreCliente() : "",
                "telefonoCliente", c.getTelefonoCliente() != null ? c.getTelefonoCliente() : "",
                "emailCliente", c.getEmailCliente() != null ? c.getEmailCliente() : "",
                "metodoPago", pagoDao.findByVentaId(v.getId()).map(Pago::getMetodo).orElse("EFECTIVO"),
                "total", v.getTotal()
        );
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Producto", "Categoría", "Cantidad", "Precio Unit.", "Subtotal")
                .forEach(title -> {
                    PdfPCell header = new PdfPCell();
                    header.setBorder(2);
                    header.setPhrase(new Phrase(title));
                    header.setBackgroundColor(BaseColor.MAGENTA);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        switch (type) {
            case "Header":
                Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                hf.setStyle(Font.BOLD);
                return hf;
            case "Data":
                Font df = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                df.setStyle(Font.BOLD);
                return df;
        }
        return new Font();
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private byte[] getByteArray(String filePath) throws Exception {
        InputStream targetStream = new FileInputStream(new File(filePath));
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }
}
