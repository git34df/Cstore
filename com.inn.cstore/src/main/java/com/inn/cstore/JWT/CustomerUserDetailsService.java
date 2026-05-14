package com.inn.cstore.JWT;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.inn.cstore.dao.UsuarioDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    UsuarioDao usuarioDao;

    private com.inn.cstore.POJO.Usuario usuario;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername: {}", username);
        usuario = usuarioDao.findByEmail(username);

        if (!Objects.isNull(usuario)) {
            // Ahora el rol viene de la relación JPA (tabla rol), no de un String libre
            String rolNombre = usuario.getRol().getNombre(); // "admin" o "usuario"
            return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                java.util.List.of(new SimpleGrantedAuthority(rolNombre))
            );
        } else {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
    }

    public com.inn.cstore.POJO.Usuario getUserDetail() {
        return usuario;
    }

}
