package com.inn.cstore.servicelmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Bill;
import com.inn.cstore.POJO.Cliente;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.BillDao;
import com.inn.cstore.dao.ClienteDao;
import com.inn.cstore.service.ClienteService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.ClienteWrapper;
import com.inn.cstore.wrapper.ClienteWrapper.FacturaResumenWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteDao clienteDao;

    @Autowired
    private BillDao billDao;

    @Autowired
    private JwtFilter jwtFilter;

    // ─────────────────────────────────────────────────────────
    // GET ALL CLIENTES — admin ve totales, usuario ve datos básicos
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<List<ClienteWrapper>> getAllClientes() {
        log.info("Inside getAllClientes");
        try {
            List<Cliente> clientes = clienteDao.getAllClientes();

            if (jwtFilter.isAdmin()) {
                // Admin: incluye totales de facturas, unidades y dinero
                List<Bill> todasFacturas = billDao.getAllBills();

                List<ClienteWrapper> result = clientes.stream().map(c -> {
                    List<Bill> facturasCli = todasFacturas.stream()
                        .filter(b -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(b.getEmail()))
                        .collect(Collectors.toList());

                    int totalFacturas  = facturasCli.size();
                    int totalUnidades  = calcularTotalUnidades(facturasCli);
                    double totalDinero = facturasCli.stream()
                        .mapToDouble(b -> b.getTotalConIgv() != null ? b.getTotalConIgv() : 0.0)
                        .sum();

                    return new ClienteWrapper(
                        c.getId(), c.getNombre(), c.getEmail(),
                        c.getRuc(), c.getRazonSocial(), c.getTelefono(), c.getDireccion(),
                        totalFacturas, totalUnidades, Math.round(totalDinero * 100.0) / 100.0
                    );
                }).collect(Collectors.toList());

                return new ResponseEntity<>(result, HttpStatus.OK);

            } else {
                // Usuario normal: solo datos básicos para poblar el dropdown de ventas
                List<ClienteWrapper> result = clientes.stream()
                    .map(c -> new ClienteWrapper(
                        c.getId(), c.getNombre(), c.getEmail(),
                        c.getRuc(), c.getRazonSocial(), c.getTelefono(), c.getDireccion(),
                        0, 0, 0.0
                    ))
                    .collect(Collectors.toList());

                return new ResponseEntity<>(result, HttpStatus.OK);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // GET RESUMEN DE UN CLIENTE — solo admin
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<ClienteWrapper> getClienteResumen(Integer id) {
        log.info("Inside getClienteResumen id={}", id);
        try {
            if (!jwtFilter.isAdmin()) {
                return new ResponseEntity<>(new ClienteWrapper(), HttpStatus.UNAUTHORIZED);
            }

            Optional<Cliente> optional = clienteDao.findById(id);
            if (optional.isEmpty()) {
                return new ResponseEntity<>(new ClienteWrapper(), HttpStatus.NOT_FOUND);
            }

            Cliente c = optional.get();
            List<Bill> todasFacturas = billDao.getAllBills();

            List<Bill> facturasCli = todasFacturas.stream()
                .filter(b -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(b.getEmail()))
                .collect(Collectors.toList());

            int    totalFacturas  = facturasCli.size();
            int    totalUnidades  = calcularTotalUnidades(facturasCli);
            double totalDinero    = facturasCli.stream()
                .mapToDouble(b -> b.getTotalConIgv() != null ? b.getTotalConIgv() : 0.0)
                .sum();

            // Mapear historial de facturas
            List<FacturaResumenWrapper> historial = facturasCli.stream().map(b ->
                new FacturaResumenWrapper(
                    b.getId(), b.getUuid(),
                    b.getSerie(), b.getCorrelativo(),
                    b.getTotalConIgv(), b.getMetodo_pago(), b.getCreatedby()
                )
            ).collect(Collectors.toList());

            ClienteWrapper wrapper = new ClienteWrapper(
                c.getId(), c.getNombre(), c.getEmail(),
                c.getRuc(), c.getRazonSocial(), c.getTelefono(), c.getDireccion(),
                totalFacturas, totalUnidades, Math.round(totalDinero * 100.0) / 100.0
            );
            wrapper.setFacturas(historial);

            return new ResponseEntity<>(wrapper, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ClienteWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // ADD CLIENTE — solo admin
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<String> addCliente(Map<String, Object> requestMap) {
        log.info("Inside addCliente");
        try {
            if (!jwtFilter.isAdmin()) {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            if (!validateClienteMap(requestMap)) {
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String email = (String) requestMap.get("email");
            if (clienteDao.findByEmail(email).isPresent()) {
                return CstoreUtils.getResponseEntity("El email ya está registrado", HttpStatus.BAD_REQUEST);
            }

            clienteDao.save(buildCliente(requestMap, null));
            return CstoreUtils.getResponseEntity("Cliente agregado exitosamente", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE CLIENTE — solo admin
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<String> updateCliente(Map<String, Object> requestMap) {
        log.info("Inside updateCliente");
        try {
            if (!jwtFilter.isAdmin()) {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            if (!requestMap.containsKey("id")) {
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Integer id = Integer.parseInt(requestMap.get("id").toString());
            Optional<Cliente> optional = clienteDao.findById(id);
            if (optional.isEmpty()) {
                return CstoreUtils.getResponseEntity("Cliente no encontrado", HttpStatus.NOT_FOUND);
            }

            clienteDao.save(buildCliente(requestMap, optional.get()));
            return CstoreUtils.getResponseEntity("Cliente actualizado exitosamente", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE CLIENTE — solo admin
    // ─────────────────────────────────────────────────────────
    @Override
    public ResponseEntity<String> deleteCliente(Integer id) {
        log.info("Inside deleteCliente id={}", id);
        try {
            if (!jwtFilter.isAdmin()) {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            Optional<Cliente> optional = clienteDao.findById(id);
            if (optional.isEmpty()) {
                return CstoreUtils.getResponseEntity("Cliente no encontrado", HttpStatus.NOT_FOUND);
            }

            clienteDao.deleteById(id);
            return CstoreUtils.getResponseEntity("Cliente eliminado exitosamente", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS PRIVADOS
    // ─────────────────────────────────────────────────────────

    private boolean validateClienteMap(Map<String, Object> map) {
        return map.containsKey("nombre") && map.containsKey("email");
    }

    private Cliente buildCliente(Map<String, Object> map, Cliente existing) {
        Cliente c = (existing != null) ? existing : new Cliente();
        if (map.containsKey("nombre"))      c.setNombre(map.get("nombre").toString());
        if (map.containsKey("email"))       c.setEmail(map.get("email").toString());
        if (map.containsKey("ruc"))         c.setRuc(map.get("ruc").toString());
        if (map.containsKey("razonSocial")) c.setRazonSocial(map.get("razonSocial").toString());
        if (map.containsKey("telefono"))    c.setTelefono(map.get("telefono").toString());
        if (map.containsKey("direccion"))   c.setDireccion(map.get("direccion").toString());
        return c;
    }

    /**
     * Calcula el total de unidades sumando los campos "cantidad" del JSON
     * productodetail de cada factura.
     */
    private int calcularTotalUnidades(List<Bill> facturas) {
        int total = 0;
        for (Bill b : facturas) {
            try {
                if (b.getProductodetail() != null && !b.getProductodetail().isBlank()) {
                    org.json.JSONArray arr = new org.json.JSONArray(b.getProductodetail());
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject obj = arr.getJSONObject(i);
                        if (obj.has("cantidad")) {
                            total += (int) obj.getDouble("cantidad");
                        }
                    }
                }
            } catch (Exception ignored) {
                // Si el JSON está malformado, se ignora esa factura
            }
        }
        return total;
    }
}