package com.athletiq.backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100)
    private String nombre;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
