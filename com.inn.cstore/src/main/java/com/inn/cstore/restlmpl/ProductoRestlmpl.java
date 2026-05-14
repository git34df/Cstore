package com.inn.cstore.restlmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.rest.ProductoRest;
import com.inn.cstore.service.ProductoService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.ProductoWrapper;

@RestController
public class ProductoRestlmpl implements ProductoRest {

    @Autowired
    ProductoService productoService;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> RequestMap) {

        try {

            return productoService.addNewProduct(RequestMap);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductoWrapper>> getAllProduct() {

        try{

            return productoService.getAllProduct();

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> RequestMap) {

        try {

            return productoService.updateProduct(RequestMap);
            
        } catch (Exception e) {
           e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {

        try {

            return productoService.deleteProduct(id);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> RequestMap) {

        try{

            return productoService.updateStatus(RequestMap);

        }catch(Exception e){
            e.printStackTrace();
        }


        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductoWrapper>> getByCategory(Integer id) {

        try {

            return productoService.getByCategory(id);
            
        } catch (Exception e) {
           e.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);


    }

    @Override
    public ResponseEntity<ProductoWrapper> getProductById(Integer id) {

        try {

            return productoService.getProductById(id);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ProductoWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
