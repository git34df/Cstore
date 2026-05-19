package com.inn.cstore.restlmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.rest.ClienteRest;
import com.inn.cstore.service.ClienteService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.ClienteWrapper;

@RestController
public class ClienteRestlmpl implements ClienteRest {

    @Autowired
    ClienteService clienteService;

    @Override
    public ResponseEntity<List<ClienteWrapper>> getAllClientes() {
        try {
            return clienteService.getAllClientes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ClienteWrapper> getClienteResumen(Integer id) {
        try {
            return clienteService.getClienteResumen(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ClienteWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> addCliente(Map<String, Object> requestMap) {
        try {
            return clienteService.addCliente(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCliente(Map<String, Object> requestMap) {
        try {
            return clienteService.updateCliente(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCliente(Integer id) {
        try {
            return clienteService.deleteCliente(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}