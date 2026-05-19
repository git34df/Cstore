package com.inn.cstore.servicelmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.POJO.Categoria;
import com.inn.cstore.POJO.Producto;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.ProductoDao;
import com.inn.cstore.service.ProductoService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.wrapper.ProductoWrapper;


@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    ProductoDao productoDao;

    @Autowired
    JwtFilter jwtFilter;


    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try{

            if(jwtFilter.isAdmin()){

                if(validateProductMap(requestMap, false)){
                    productoDao.save(getProductFromMap(requestMap, false));
                    return CstoreUtils.getResponseEntity("Producto added Successfully", HttpStatus.OK);
                }
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }else{
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {

        // Validar nombre
        if (!requestMap.containsKey("nombre_producto")) {
            return false;
        }

        // Validar id cuando se requiere
        if (validateId && !requestMap.containsKey("id_producto")) {
            return false;
        }

        // Validar stock siempre
        if (!requestMap.containsKey("stock")) {
            return false;
        }

        // Si pasó todas las validaciones
        return true;
    }


    
    private Producto getProductFromMap(Map<String,String> requestMap, boolean isAdd) {
        Categoria categoria = new Categoria();

        categoria.setId(Integer.parseInt(requestMap.get("IdCategoria")));

        Producto producto = new Producto();
        if(isAdd){
            producto.setId(Integer.parseInt(requestMap.get("id_producto")));
        }else{
            producto.setStatus("true");
        }

        producto.setCategoria(categoria);

        producto.setNombre(requestMap.get("nombre_producto"));

        producto.setDescription(requestMap.get("descripcion"));

        producto.setPrice(Integer.parseInt(requestMap.get("precio")));

        producto.setStock(Integer.parseInt(requestMap.get("stock")));

        return producto;


    }



    @Override
    public ResponseEntity<List<ProductoWrapper>> getAllProduct() {
       try {

        return new ResponseEntity<>(productoDao.getAllProduct(), HttpStatus.OK);
        
       } catch (Exception e) {
        e.printStackTrace();
       }

       return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> RequestMap) {

        try {

            if(jwtFilter.isAdmin()){

                if(validateProductMap(RequestMap, true)){

                    Optional <Producto> optional = productoDao.findById(Integer.parseInt(RequestMap.get("id_producto")));

                    if(!optional.isEmpty()){

                        Producto producto = getProductFromMap(RequestMap, true);
                        producto.setStatus(optional.get().getStatus());
                        productoDao.save(producto);
                        return CstoreUtils.getResponseEntity("Product Updated Successfully", HttpStatus.OK);

                    }else{
                        return CstoreUtils.getResponseEntity("Product id Does not exist", HttpStatus.OK);
                    }


                }else{
                    return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }

            }else{
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {

        try {

            if(jwtFilter.isAdmin()){

                Optional optional = productoDao.findById(id);
                if(!optional.isEmpty()){

                    productoDao.deleteById(id);

                    return CstoreUtils.getResponseEntity("Product Deleted Successfully", HttpStatus.OK);

                }
                return CstoreUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);

            }else{
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }



    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> RequestMap) {


        try {

            if (jwtFilter.isAdmin()) {

                Optional optional = productoDao.findById(Integer.parseInt(RequestMap.get("id_producto")));
                
                if(!optional.isEmpty()){

                    productoDao.updateProductStatus(RequestMap.get("status"), Integer.parseInt(RequestMap.get("id_producto")));

                    return CstoreUtils.getResponseEntity("Product Status Update Succesfully", HttpStatus.OK);

                }
                return CstoreUtils.getResponseEntity("Product id does not exist", HttpStatus.OK);

                
            }else{
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);


    }



    @Override
    public ResponseEntity<List<ProductoWrapper>> getByCategory(Integer id) {

        try {

            return new ResponseEntity<>(productoDao.getProductByCategory(id),HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);

    }



    @Override
    public ResponseEntity<ProductoWrapper> getProductById(Integer id) {
        

        try {

            return new ResponseEntity<>(productoDao.getProductById(id),HttpStatus.OK);            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ProductoWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);


    }


}
