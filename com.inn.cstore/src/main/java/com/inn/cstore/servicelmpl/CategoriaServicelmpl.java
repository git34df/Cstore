package com.inn.cstore.servicelmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Categoria;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.CategoriaDao;
import com.inn.cstore.service.CategoriaService;
import com.inn.cstore.utils.CstoreUtils;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class CategoriaServicelmpl implements CategoriaService {


    @Autowired
    CategoriaDao categoriaDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {

        try{

            if(jwtFilter.isAdmin()){

                if(validateCategoryMap(requestMap, false)){
                    categoriaDao.save(getCategoryFromMap(requestMap, false));
                    return CstoreUtils.getResponseEntity("Categoria added Successfully", HttpStatus.OK);
                }

            }
            else{
                return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.UNAUTHORIZED);
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private boolean validateCategoryMap(Map<String,String> requestMap, boolean validateId) {

        if(requestMap.containsKey("nombre")){
            if(requestMap.containsKey("IdCategoria") && validateId){
                return true;
            }else if (!validateId){
                return true;
            }
        }

        return false;

    }

    private Categoria getCategoryFromMap(Map<String, String> requestMap, boolean isAdd){
        Categoria categoria = new Categoria();

        
        if (!isAdd && requestMap.containsKey("IdCategoria")) {
            categoria.setId(Integer.parseInt(requestMap.get("IdCategoria")));
        }

        categoria.setNombre(requestMap.get("nombre"));
        return categoria;
    }

    @Override
    public ResponseEntity<List<Categoria>> getAllCategory(String filterValue) {

        try{

            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")){
                log.info("inside if");
                return new ResponseEntity<List<Categoria>>(categoriaDao.getAllCategoria(), HttpStatus.OK);
            }

            return new ResponseEntity<>(categoriaDao.findAll(), HttpStatus.OK);

        }catch(Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<List<Categoria>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {

        System.out.println(">>> JSON recibido en updateCategory: " + requestMap);

        try {

            if(jwtFilter.isAdmin()){

                if(validateCategoryMap(requestMap, true)){
                    Optional optional = categoriaDao.findById(Integer.parseInt(requestMap.get("IdCategoria")));

                    if(!optional.isEmpty()){

                        System.out.println("Objeto antes de guardar: " + getCategoryFromMap(requestMap, true));
                        log.info("Actualizando categoría ID={} con nombre={}", requestMap.get("IdCategoria"), requestMap.get("nombre"));
                        categoriaDao.save(getCategoryFromMap(requestMap, false));
                        return CstoreUtils.getResponseEntity("Categoria Updated Successfully",HttpStatus.OK);

                    }else{
                        return CstoreUtils.getResponseEntity("Categoria id does not exist", HttpStatus.OK);
                    }

                }
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }else{
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
