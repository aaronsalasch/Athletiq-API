package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.request.LoginRequest;
import com.athletiq.backend.dtos.request.RegisterRequest;
import com.athletiq.backend.dtos.response.AuthResponse;
import com.athletiq.backend.exceptions.ConflictException;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.Rol;
import com.athletiq.backend.models.entities.Usuario;
import com.athletiq.backend.repositories.RolRepository;
import com.athletiq.backend.repositories.UsuarioRepository;
import com.athletiq.backend.security.JwtService;
import com.athletiq.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Delega la validación de credenciales a Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.getCorreo()));

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return buildAuthResponse(usuario, generateToken(usuario));
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new ConflictException("El correo '" + request.getCorreo() + "' ya está registrado");
        }

        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "USUARIO"));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rolUsuario)
                .build();

        usuarioRepository.save(usuario);

        return buildAuthResponse(usuario, generateToken(usuario));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String generateToken(Usuario usuario) {
        UserDetails userDetails = new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()))
        );
        return jwtService.generateToken(userDetails);
    }

    private AuthResponse buildAuthResponse(Usuario usuario, String token) {
        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .avatarUrl(usuario.getAvatarUrl())
                .nivel(usuario.getNivel())
                .puntosXp(usuario.getPuntosXp())
                .rachaActual(usuario.getRachaActual())
                .colorHexLiga(usuario.getColorHexLiga())
                .rol(usuario.getRol().getNombre())
                .build();
    }
}
