package com.athletiq.backend.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarPerfilRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    // URL del avatar (validación básica de longitud)
    @Size(max = 500, message = "La URL del avatar no puede superar los 500 caracteres")
    private String avatarUrl;

    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;
}
