package br.com.fiap3ess.autoescola3ess.controller;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsSwaggerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("CORS: preflight de origem permitida retorna os cabeçalhos Access-Control-*")
    void corsOrigemPermitida() throws Exception {
        mockMvc.perform(options("/instrutores")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    @DisplayName("CORS: preflight de origem não permitida é rejeitado (403)")
    void corsOrigemNaoPermitida() throws Exception {
        mockMvc.perform(options("/instrutores")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Swagger: documentação OpenAPI é publicada sem autenticação e lista os endpoints")
    void swaggerApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/instrutores")))
                .andExpect(content().string(containsString("/alunos")))
                .andExpect(content().string(containsString("/usuarios")))
                .andExpect(content().string(containsString("/instrucoes")))
                .andExpect(content().string(containsString("/enderecos/cep/{cep}")))
                .andExpect(content().string(containsString("bearer-key")));
    }
}
