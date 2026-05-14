package com.inn.cstore.restlmpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.rest.VentaRest;
import com.inn.cstore.service.VentaService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.VentaWrapper;

@RestController
public class VentaRestlmpl implements VentaRest {

    @Autowired
    VentaService ventaService;

    @Override
    public ResponseEntity<String> registrarVenta(Map<String, Object> requestMap) {
        try {
            return ventaService.registrarVenta(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<VentaWrapper>> getVentas() {
        try {
            return ventaService.getVentas();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<VentaWrapper> getVentaById(Integer id) {
        try {
            return ventaService.getVentaById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        try {
            return ventaService.getPdf(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> anularVenta(Integer id) {
        try {
            return ventaService.anularVenta(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
