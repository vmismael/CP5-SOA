package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Usuario;
import br.com.fiap3ess.autoescola3ess.application.port.out.UsuarioRepository;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Role;
import com.jayway.jsonpath.JsonPath;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
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
@Transactional
class LoginControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Login válido retorna token JWT que permite acessar endpoint protegido")
    void loginValido() throws Exception {
        repository.save(new Usuario("admin.teste", passwordEncoder.encode("senha123"), Role.ADMIN));

        String resposta = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin.teste\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenJWT").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(resposta, "$.tokenJWT");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/instrutores").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login com senha errada retorna 401")
    void loginInvalido() throws Exception {
        repository.save(new Usuario("admin.teste", passwordEncoder.encode("senha123"), Role.ADMIN));

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin.teste\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login de usuário inexistente retorna 401")
    void loginUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"naoexiste\",\"senha\":\"qualquer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token inválido retorna 401")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/instrutores").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }
}
