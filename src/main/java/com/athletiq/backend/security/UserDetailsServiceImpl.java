package com.athletiq.backend.security;

import com.athletiq.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreo(correo)
                .map(usuario -> new User(
                        usuario.getCorreo(),
                        usuario.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
    }
}
