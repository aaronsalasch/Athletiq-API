package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.request.LoginRequest;
import com.athletiq.backend.dtos.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller — Integración")
class AuthControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register → 201 + token cuando los datos son válidos")
    void register_validData_returns201WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Atleta Test");
        req.setCorreo("atleta@test.com");
        req.setPassword("secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.correo", is("atleta@test.com")))
                .andExpect(jsonPath("$.nombre", is("Atleta Test")))
                .andExpect(jsonPath("$.nivel", is(1)))
                .andExpect(jsonPath("$.puntosXp", is(0)));
    }

    @Test
    @DisplayName("POST /api/auth/register → 409 cuando el correo ya existe")
    void register_duplicateEmail_returns409() throws Exception {
        // Primer registro
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Usuario A");
        req.setCorreo("duplicado@test.com");
        req.setPassword("pass123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Segundo registro con el mismo correo
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("duplicado@test.com")));
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 cuando el cuerpo está incompleto")
    void register_missingFields_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setCorreo("incompleto@test.com");
        // falta nombre y password

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login → 200 + token cuando las credenciales son correctas")
    void login_validCredentials_returns200WithToken() throws Exception {
        // Crear usuario primero
        RegisterRequest reg = new RegisterRequest();
        reg.setNombre("Login Test");
        reg.setCorreo("login@test.com");
        reg.setPassword("mipassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Intentar login
        LoginRequest login = new LoginRequest();
        login.setCorreo("login@test.com");
        login.setPassword("mipassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.correo", is("login@test.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login → 401 cuando la contraseña es incorrecta")
    void login_wrongPassword_returns401() throws Exception {
        // Crear usuario
        RegisterRequest reg = new RegisterRequest();
        reg.setNombre("Wrong Pass");
        reg.setCorreo("wrong@test.com");
        reg.setPassword("correctpass");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Login con contraseña incorrecta
        LoginRequest login = new LoginRequest();
        login.setCorreo("wrong@test.com");
        login.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
