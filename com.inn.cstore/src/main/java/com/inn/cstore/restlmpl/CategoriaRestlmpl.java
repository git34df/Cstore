package com.inn.cstore.restlmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cstore.POJO.Categoria;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.rest.CategoriaRest;
import com.inn.cstore.service.CategoriaService;
import com.inn.cstore.utils.CstoreUtils;


@RestController
public class CategoriaRestlmpl implements CategoriaRest {


    @Autowired
    CategoriaService categoriaService;



    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
       try{

        return categoriaService.addNewCategory(requestMap);

       }catch(Exception e){
        e.printStackTrace();
       }
       return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Override
    public ResponseEntity<List<Categoria>> getAllCategory(String filterValue) {

        try{

            return categoriaService.getAllCategory(filterValue);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<> (new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }



    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {

        try {

            return categoriaService.updateCategory(requestMap);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
