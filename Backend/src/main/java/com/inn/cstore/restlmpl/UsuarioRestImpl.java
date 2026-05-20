package com.inn.cstore.restlmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.rest.UsuarioRest;
import com.inn.cstore.service.UsuarioService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.UsuarioWrapper;



@RestController
public class UsuarioRestImpl implements UsuarioRest {

    @Autowired
    UsuarioService usuarioservice;

    @Override
    public ResponseEntity<String> SingUp(@RequestBody Map<String, String> RequestMap) {
        try{
            return usuarioservice.singUp(RequestMap);
        }catch (Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> Login(@RequestBody Map<String, String> RequestMap) {
        try {
            return usuarioservice.Login(RequestMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UsuarioWrapper>> getAllUsuarios() {
        try{
            
            return usuarioservice.getAllUsuarios();  

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<List<UsuarioWrapper>>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> RequestMap) {

        try{
            
            return usuarioservice.update(RequestMap);

        }catch(Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> checkToken() {
        try{

            return usuarioservice.checkToken();

        }catch ( Exception e){
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);


    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {

        try{

            return usuarioservice.changePassword(requestMap);

        }catch(Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {

        try{

            return usuarioservice.forgotPassword(requestMap);

        }catch(Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateRol(Map<String, String> requestMap) {
        try {
            return usuarioservice.updateRol(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        try {
            return usuarioservice.resetPassword(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
