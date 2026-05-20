package com.inn.cstore.servicelmpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.inn.cstore.JWT.CustomerUserDetailsService;
import com.inn.cstore.JWT.JwtFilter;
import com.inn.cstore.JWT.JwtUtil;
import com.inn.cstore.POJO.Rol;
import com.inn.cstore.POJO.Usuario;
import com.inn.cstore.constents.CstoreConstants;
import com.inn.cstore.dao.RolDao;
import com.inn.cstore.dao.UsuarioDao;
import com.inn.cstore.service.UsuarioService;
import com.inn.cstore.utils.CstoreUtils;
import com.inn.cstore.utils.EmailUtils;
import com.inn.cstore.wrapper.UsuarioWrapper;
import com.inn.cstore.dao.RolDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    UsuarioDao usuarioDao;

    @Autowired
    RolDao rolDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> singUp(Map<String, String> requestMap) {

        log.info("Inside singUp {}", requestMap);
        try {
            if (validateSingUpMap(requestMap)) {

                Usuario usuario = usuarioDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(usuario)) {
                    usuarioDao.save(getUsuarioFromMap(requestMap));
                    return CstoreUtils.getResponseEntity("Succesfully Registered", HttpStatus.OK);
                } else {
                    return CstoreUtils.getResponseEntity("Email ya creado", HttpStatus.BAD_REQUEST);
                }

            } else {
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSingUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("nombre") &&
                requestMap.containsKey("numerocontacto") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("contraseña");
    }

    private Usuario getUsuarioFromMap(Map<String, String> requestMap) {
        // Buscar el rol "usuario" desde la BD
        Rol rolUsuario = rolDao.findByNombre("usuario")
                .orElseThrow(() -> new RuntimeException("Rol 'usuario' no encontrado en BD"));

        Usuario usuario = new Usuario();
        usuario.setNombre(requestMap.get("nombre"));
        usuario.setNumerotelefono(requestMap.get("numerocontacto"));
        usuario.setEmail(requestMap.get("email"));
        usuario.setPassword(requestMap.get("contraseña"));
        usuario.setEstado("false");
        usuario.setRol(rolUsuario);
        return usuario;
    }

    @Override
    public ResponseEntity<String> Login(Map<String, String> requestMap) {

        log.info("Inside Login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));

            if (auth.isAuthenticated()) {
                if (customerUserDetailsService.getUserDetail().getEstado().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>("{\"token\":\"" +
                            jwtUtil.generateToken(
                                    customerUserDetailsService.getUserDetail().getEmail(),
                                    // Ahora el rol viene de la relación JPA, no de un String directo
                                    customerUserDetailsService.getUserDetail().getRol().getNombre())
                            + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval." + "\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }

        } catch (Exception e) {
            log.error("{}", e);
        }

        return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UsuarioWrapper>> getAllUsuarios() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(usuarioDao.getAllUsuarios(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Usuario> optional = usuarioDao.findById(Integer.parseInt(requestMap.get("id")));

                if (!optional.isEmpty()) {
                    usuarioDao.updateStatus(requestMap.get("estado"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("estado"), optional.get().getEmail(), usuarioDao.getAllAdmin());
                    return CstoreUtils.getResponseEntity("User status updated succesfully", HttpStatus.OK);
                } else {
                    CstoreUtils.getResponseEntity("Id de usuario inexistente", HttpStatus.OK);
                }

            } else {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUserName());
        if (status != null && status.equalsIgnoreCase("true")) {
            emailUtils.sendSimpleMessage(
                    jwtFilter.getCurrentUserName(),
                    "Cuenta aprobada",
                    "USER:-" + user + "\n es aprobado por \n ADMIN:-" + jwtFilter.getCurrentUserName(),
                    allAdmin);
        } else {
            emailUtils.sendSimpleMessage(
                    jwtFilter.getCurrentUserName(),
                    "Cuenta deshabilitada",
                    "USER:-" + user + "\n es deshabilitado por \n ADMIN:-" + jwtFilter.getCurrentUserName(),
                    allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CstoreUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            Usuario usuarioObj = usuarioDao.findByEmail(jwtFilter.getCurrentUserName());
            if (usuarioObj != null) {
                if (usuarioObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    usuarioObj.setPassword(requestMap.get("newPassword"));
                    usuarioDao.save(usuarioObj);
                    return CstoreUtils.getResponseEntity("Password Updated Succesfully", HttpStatus.OK);
                }
                return CstoreUtils.getResponseEntity("Contraseña antigua incorrecta", HttpStatus.BAD_REQUEST);
            }

            return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            Usuario usuario = usuarioDao.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(usuario) && !Strings.isNullOrEmpty(usuario.getEmail())) {
                emailUtils.forgotMail(usuario.getEmail(), "Credenciales por CheapStore", usuario.getPassword());
            }
            return CstoreUtils.getResponseEntity("Check your email for credentials", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateRol(Map<String, String> requestMap) {
        try {
            if (!jwtFilter.isAdmin()) {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED);
            }

            if (!requestMap.containsKey("id") || !requestMap.containsKey("rol")) {
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST);
            }

            Integer userId = Integer.parseInt(requestMap.get("id"));
            String rolNombre = requestMap.get("rol"); // "admin" o "usuario"

            Rol rol = rolDao.findByNombre(rolNombre)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));

            Optional<Usuario> optional = usuarioDao.findById(userId);
            if (optional.isEmpty()) {
                return CstoreUtils.getResponseEntity("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            usuarioDao.updateRol(rol.getId(), userId);
            return CstoreUtils.getResponseEntity("Rol actualizado correctamente", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        try {
            if (!jwtFilter.isAdmin()) {
                return CstoreUtils.getResponseEntity(CstoreConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            if (!requestMap.containsKey("id") || !requestMap.containsKey("newPassword")) {
                return CstoreUtils.getResponseEntity(CstoreConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Optional<Usuario> optional = usuarioDao.findById(Integer.parseInt(requestMap.get("id")));
            if (optional.isEmpty()) {
                return CstoreUtils.getResponseEntity("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            Usuario usuario = optional.get();
            usuario.setPassword(requestMap.get("newPassword"));
            usuarioDao.save(usuario);
            return CstoreUtils.getResponseEntity("Contraseña reseteada correctamente", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CstoreUtils.getResponseEntity(CstoreConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}