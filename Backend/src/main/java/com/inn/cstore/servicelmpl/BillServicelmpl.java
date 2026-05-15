package com.inn.cstore.servicelmpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Bill;
import com.inn.cstore.POJO.Producto;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.BillDao;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.service.BillService;
import com.inn.cstore.utils.CstoreUtils;
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
public class BillServicelmpl implements BillService {

    // ── Datos del emisor (CheapStore) ─────────────────────────
    private static final String EMISOR_RUC          = "20123456789";
    private static final String EMISOR_RAZON_SOCIAL = "CHEAPSTORE S.A.C.";
    private static final String EMISOR_DIRECCION    = "Av. Principal 123, Lima, Perú";
    private static final String EMISOR_SERIE        = "F001";
    private static final double IGV_RATE            = 0.18;

    @Autowired JwtFilter    jwtFilter;
    @Autowired BillDao      billDao;
    @Autowired ProductoDao  productoDao;

    // ─────────────────────────────────────────────────────────
    // GENERAR FACTURA (SUNAT)
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport (SUNAT)");
        try {
            if (!validateRequestMap(requestMap)) {
                return CstoreUtils.getResponseEntity("Required Data not found", HttpStatus.BAD_REQUEST);
            }

            String fileName;
            boolean isGenerate = !requestMap.containsKey("isGenerate") || (Boolean) requestMap.get("isGenerate");

            if (!isGenerate) {
                fileName = (String) requestMap.get("uuid");
            } else {
                fileName = CstoreUtils.getUUID();
                requestMap.put("uuid", fileName);
                insertBill(requestMap);
                descontarStock(requestMap);
            }

            // ── Cálculo de montos ─────────────────────────────
            double totalConIgv = Double.parseDouble(requestMap.get("total").toString());
            double subtotal    = Math.round((totalConIgv / (1 + IGV_RATE)) * 100.0) / 100.0;
            double igv         = Math.round((totalConIgv - subtotal) * 100.0) / 100.0;

            // ── Correlativo ───────────────────────────────────
            int correlativo = billDao.count() > 0
                    ? (int) billDao.count()
                    : 1;
            String numeroComprobante = EMISOR_SERIE + "-" + String.format("%08d", correlativo);

            // ── Datos del cliente ─────────────────────────────
            String rucCliente    = getStr(requestMap, "ruc_cliente",       "Sin RUC");
            String razonSocial   = getStr(requestMap, "razon_social",      getStr(requestMap, "name", "-"));
            String direccionCli  = getStr(requestMap, "direccion_cliente", "-");
            String email         = getStr(requestMap, "email",             "-");
            String telefono      = getStr(requestMap, "numero_contacto",   "-");
            String metodoPago    = getStr(requestMap, "metodo_pago",       "-");
            String fecha         = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // ── Crear directorio si no existe (fix Railway) ───
            File storeDir = new File(CstoreConstants.STORE_LOCATION);
            if (!storeDir.exists()) {
                storeDir.mkdirs();
            }

            // ── Generar PDF ───────────────────────────────────
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream(CstoreConstants.STORE_LOCATION + "/" + fileName + ".pdf"));
            document.open();

            // Borde exterior
            setRectangleInPdf(document);

            // ── ENCABEZADO ────────────────────────────────────
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});
            headerTable.setSpacingAfter(10f);

            // Celda emisor
            PdfPCell emisorCell = new PdfPCell();
            emisorCell.setBorder(Rectangle.BOX);
            emisorCell.setPadding(8f);
            emisorCell.addElement(new Paragraph(EMISOR_RAZON_SOCIAL,  getFont("SubHeader")));
            emisorCell.addElement(new Paragraph("RUC: " + EMISOR_RUC, getFont("Data")));
            emisorCell.addElement(new Paragraph(EMISOR_DIRECCION,     getFont("Data")));
            emisorCell.addElement(new Paragraph("Lima, Perú",         getFont("Data")));
            headerTable.addCell(emisorCell);

            // Celda comprobante (derecha) — recuadro obligatorio SUNAT
            PdfPCell comprobanteCell = new PdfPCell();
            comprobanteCell.setBorder(Rectangle.BOX);
            comprobanteCell.setPadding(8f);
            comprobanteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            comprobanteCell.addElement(buildCenteredParagraph("RUC: " + EMISOR_RUC,  getFont("Data")));
            comprobanteCell.addElement(buildCenteredParagraph("FACTURA ELECTRÓNICA", getFont("SubHeader")));
            comprobanteCell.addElement(buildCenteredParagraph(numeroComprobante,     getFont("SubHeader")));
            headerTable.addCell(comprobanteCell);

            document.add(headerTable);

            // ── DATOS DEL CLIENTE ─────────────────────────────
            PdfPTable clienteTable = new PdfPTable(1);
            clienteTable.setWidthPercentage(100);
            clienteTable.setSpacingAfter(8f);

            PdfPCell clienteCell = new PdfPCell();
            clienteCell.setBorder(Rectangle.BOX);
            clienteCell.setPadding(7f);
            clienteCell.addElement(new Paragraph("Señor(es): " + razonSocial, getFont("Data")));
            clienteCell.addElement(new Paragraph("RUC / DNI: " + rucCliente,  getFont("Data")));
            clienteCell.addElement(new Paragraph("Dirección: " + direccionCli, getFont("Data")));
            clienteCell.addElement(new Paragraph(
                    "Correo: " + email + "    Teléfono: " + telefono, getFont("Data")));
            clienteCell.addElement(new Paragraph(
                    "Fecha de emisión: " + fecha +
                    "    Fecha de vencimiento: " + fecha +
                    "    Moneda: Soles (PEN)", getFont("Data")));
            clienteCell.addElement(new Paragraph(
                    "Condición de pago: " + metodoPago.toUpperCase(), getFont("Data")));
            clienteTable.addCell(clienteCell);

            document.add(clienteTable);

            // ── TABLA DE ÍTEMS ────────────────────────────────
            PdfPTable itemsTable = new PdfPTable(6);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{8f, 30f, 12f, 12f, 18f, 18f});
            itemsTable.setSpacingAfter(8f);
            addItemTableHeader(itemsTable);

            JSONArray jsonArray = normalizeDetalleProducto(requestMap.get("detalleproducto"));
            int itemNum = 1;
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> item = CstoreUtils.getMapFromJson(jsonArray.getJSONObject(i).toString());
                addItemRow(itemsTable, item, itemNum++, IGV_RATE);
            }
            document.add(itemsTable);

            // ── TOTALES ───────────────────────────────────────
            PdfPTable totalesTable = new PdfPTable(2);
            totalesTable.setWidthPercentage(50);
            totalesTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalesTable.setWidths(new float[]{55f, 45f});
            totalesTable.setSpacingAfter(10f);

            addTotalRow(totalesTable,     "Op. Gravadas (S/)", String.format("%.2f", subtotal));
            addTotalRow(totalesTable,     "IGV 18% (S/)",      String.format("%.2f", igv));
            addTotalRowBold(totalesTable, "TOTAL (S/)",        String.format("%.2f", totalConIgv));

            document.add(totalesTable);

            // ── LEYENDA ───────────────────────────────────────
            Paragraph leyenda = new Paragraph(
                    "Son: " + numeroALetras(totalConIgv) + " SOLES\n\n" +
                    "Representación impresa de la Factura Electrónica. " +
                    "Consulte su validez en www.sunat.gob.pe",
                    getFont("Small"));
            leyenda.setAlignment(Element.ALIGN_CENTER);
            document.add(leyenda);

            document.close();

            return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS PDF
    // ─────────────────────────────────────────────────────────

    private void addItemTableHeader(PdfPTable table) {
        BaseColor headerColor = new BaseColor(41, 98, 190);
        String[] cols = {"Ítem", "Descripción", "U.M.", "Cantidad", "V. Unitario", "Subtotal"};
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, getFont("TableHeader")));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }
    }

    private void addItemRow(PdfPTable table, Map<String, Object> item, int num, double igvRate) {
        double precio    = Double.parseDouble(String.valueOf(item.get("precio")));
        int    cantidad  = (int) Double.parseDouble(String.valueOf(item.get("cantidad")));
        double valorUnit = Math.round((precio / (1 + igvRate)) * 100.0) / 100.0;
        double subTotal  = Math.round(valorUnit * cantidad * 100.0) / 100.0;

        BaseColor alt = (num % 2 == 0) ? new BaseColor(235, 241, 255) : BaseColor.WHITE;

        addDataCell(table, String.valueOf(num),               Element.ALIGN_CENTER, alt);
        addDataCell(table, String.valueOf(item.get("nombre")), Element.ALIGN_LEFT,  alt);
        addDataCell(table, "NIU",                             Element.ALIGN_CENTER, alt);
        addDataCell(table, String.valueOf(cantidad),          Element.ALIGN_CENTER, alt);
        addDataCell(table, String.format("%.2f", valorUnit),  Element.ALIGN_RIGHT,  alt);
        addDataCell(table, String.format("%.2f", subTotal),   Element.ALIGN_RIGHT,  alt);
    }

    private void addDataCell(PdfPTable table, String text, int align, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getFont("Data")));
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(bg);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, getFont("Data")));
        lbl.setBorder(Rectangle.BOX);
        lbl.setPadding(4f);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(value, getFont("Data")));
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setBorder(Rectangle.BOX);
        val.setPadding(4f);
        table.addCell(val);
    }

    private void addTotalRowBold(PdfPTable table, String label, String value) {
        BaseColor bg = new BaseColor(41, 98, 190);

        PdfPCell lbl = new PdfPCell(new Phrase(label, getFont("TableHeader")));
        lbl.setBackgroundColor(bg);
        lbl.setPadding(5f);
        table.addCell(lbl);

        PdfPCell val = new PdfPCell(new Phrase(value, getFont("TableHeader")));
        val.setBackgroundColor(bg);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setPadding(5f);
        table.addCell(val);
    }

    private Paragraph buildCenteredParagraph(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(new BaseColor(41, 98, 190));
        rectangle.setBorderWidth(1.5f);
        document.add(rectangle);
    }

    // ─────────────────────────────────────────────────────────
    // FUENTES
    // ─────────────────────────────────────────────────────────
    private Font getFont(String type) {
        switch (type) {
            case "Header":
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE);
            case "SubHeader":
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(41, 98, 190));
            case "TableHeader":
                return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            case "Data":
                return FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            case "Small":
                return FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(100, 100, 100));
        }
        return new Font();
    }

    // ─────────────────────────────────────────────────────────
    // NÚMERO A LETRAS (simplificado para Perú)
    // ─────────────────────────────────────────────────────────
    private String numeroALetras(double monto) {
        int entero   = (int) monto;
        int centavos = (int) Math.round((monto - entero) * 100);
        return "SON " + entero + " CON " + String.format("%02d", centavos) + "/100";
    }

    // ─────────────────────────────────────────────────────────
    // LÓGICA DE NEGOCIO
    // ─────────────────────────────────────────────────────────
    private void insertBill(Map<String, Object> requestMap) {
        try {
            double totalConIgv = Double.parseDouble(requestMap.get("total").toString());
            double subtotal    = Math.round((totalConIgv / (1 + IGV_RATE)) * 100.0) / 100.0;
            double igv         = Math.round((totalConIgv - subtotal) * 100.0) / 100.0;
            int    correlativo = (int) billDao.count() + 1;

            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setSerie(EMISOR_SERIE);
            bill.setCorrelativo(correlativo);
            bill.setNombre(getStr(requestMap, "name", ""));
            bill.setEmail(getStr(requestMap, "email", ""));
            bill.setNumerocontacto(getStr(requestMap, "numero_contacto", ""));
            bill.setRucCliente(getStr(requestMap, "ruc_cliente", ""));
            bill.setRazonSocial(getStr(requestMap, "razon_social", ""));
            bill.setDireccionCliente(getStr(requestMap, "direccion_cliente", ""));
            bill.setMetodo_pago(getStr(requestMap, "metodo_pago", ""));
            bill.setSubtotal(subtotal);
            bill.setIgv(igv);
            bill.setTotalConIgv(totalConIgv);
            bill.setTotal((int) totalConIgv);
            bill.setProductodetail(String.valueOf(requestMap.get("detalleproducto")));
            bill.setCreatedby(jwtFilter.getCurrentUserName());
            billDao.save(bill);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void descontarStock(Map<String, Object> requestMap) throws JSONException {
        JSONArray jsonArray = normalizeDetalleProducto(requestMap.get("detalleproducto"));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String nombre  = obj.getString("nombre");
            int cantidad   = (int) obj.getDouble("cantidad");
            Producto producto = productoDao.findByNombre(nombre);
            if (producto == null)
                throw new RuntimeException("Producto no existe: " + nombre);
            if (producto.getStock() < cantidad)
                throw new RuntimeException("Stock insuficiente para: " + nombre);
            producto.setStock(producto.getStock() - cantidad);
            productoDao.save(producto);
        }
    }

    private JSONArray normalizeDetalleProducto(Object detalle) {
        try {
            if (detalle instanceof String)
                return CstoreUtils.getJsonArrayFromString((String) detalle);
            if (detalle instanceof List) {
                JSONArray arr = new JSONArray();
                for (Object o : (List<?>) detalle) arr.put(o);
                return arr;
            }
            throw new RuntimeException("Formato no soportado en detalleproducto");
        } catch (Exception e) {
            throw new RuntimeException("Error procesando detalleproducto", e);
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("numero_contacto") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("metodo_pago") &&
                requestMap.containsKey("detalleproducto") &&
                requestMap.containsKey("total");
    }

    private String getStr(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return (val != null && !val.toString().isBlank()) ? val.toString() : def;
    }

    // ─────────────────────────────────────────────────────────
    // ENDPOINTS RESTANTES
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = jwtFilter.isAdmin()
                ? billDao.getAllBills()
                : billDao.getBillByUserName(jwtFilter.getCurrentUserName());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        try {
            String uuid = (String) requestMap.get("uuid");
            if (uuid == null || uuid.isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            // fix Railway: separador / en lugar de \\
            String filePath = CstoreConstants.STORE_LOCATION + "/" + uuid + ".pdf";
            if (!CstoreUtils.isFileExist(filePath)) {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
            }

            byte[] byteArray = getByteArray(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("inline").filename(uuid + ".pdf").build());
            return new ResponseEntity<>(byteArray, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional<Bill> optional = billDao.findById(id);
            if (optional.isPresent()) {
                billDao.deleteById(id);
                return CstoreUtils.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
            }
            return CstoreUtils.getResponseEntity("Bill id does not exist", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filePath) throws Exception {
        InputStream targetStream = new FileInputStream(new File(filePath));
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }
}