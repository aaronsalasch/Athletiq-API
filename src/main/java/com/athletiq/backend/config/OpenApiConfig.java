package com.athletiq.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer";

    @Bean
    public OpenAPI athletiqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Athletiq API")
                        .description("""
                            API RESTful para la plataforma gamificada de aprendizaje de habilidades físicas.

                            **Modo Invitado:** los endpoints `GET /api/actividades/**`, `/api/secciones/**`
                            y `/api/habilidades/**` son públicos y no requieren token.

                            **Autenticado:** el resto de operaciones requiere un token Bearer JWT.
                            Obtenlo en `POST /api/auth/login` o `POST /api/auth/register`.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Athletiq Dev Team")
                                .email("dev@athletiq.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega el JWT sin el prefijo 'Bearer '")))
                // Aplica el esquema globalmente; los endpoints públicos igualmente funcionan sin token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
