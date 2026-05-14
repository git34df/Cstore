package com.inn.cstore.utils;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CstoreUtils {
    private CstoreUtils(){

    }

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus status){
        return new ResponseEntity<String>("{\"message\":\""+responseMessage+"\"}",status);
    } 

    public static String getUUID(){
        Date data = new Date();
        long time = data.getTime();
        return "Bill-" + time;

    }

    public static JSONArray getJsonArrayFromString(Object data) throws JSONException {
        if (data == null)
            return new JSONArray();

        // Si es un String
        if (data instanceof String) {
            String jsonStr = ((String) data).trim();
            if (jsonStr.startsWith("[")) {
                return new JSONArray(jsonStr); // ya es un array
            } else if (jsonStr.startsWith("{")) {
                // Es un objeto, lo metemos en un array
                JSONArray array = new JSONArray();
                array.put(new JSONObject(jsonStr));
                return array;
            } else {
                throw new JSONException("Invalid JSON string");
            }
        }

        // Si es una lista/colección
        if (data instanceof Collection<?>) {
            return new JSONArray((Collection<?>) data);
        }

        // Cualquier otro objeto lo convertimos a JSONObject y luego a JSONArray
        JSONArray array = new JSONArray();
        array.put(new JSONObject((Map) data));
        return array;
    }

    public static Map<String,Object> getMapFromJson(String data){
        if(!Strings.isNullOrEmpty(data))
            return new Gson().fromJson(data, new TypeToken<Map<String,Object>>(){

            }.getType());

        return new HashMap<>();
    }

    public static Boolean isFileExist(String path){

        log.info("Inside isFileExist {}", path);
        try {

            File file = new File(path);

            return (file != null && file.exists()) ? Boolean.TRUE : Boolean.FALSE;
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
