package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrutorRepository;
import br.com.fiap3ess.autoescola3ess.support.TestData;
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
class InstrutorControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    InstrutorRepository repository;

    @Test
    @DisplayName("Cadastrar: 201 com Location para dados válidos")
    @WithMockUser(roles = "ADMIN")
    void cadastrar() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.instrutorJson("novo@email.com", "01234567890")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Instrutor Novo"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("Cadastrar: 400 para dados inválidos")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInvalido() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cadastrar: 403 para perfil USER")
    @WithMockUser(roles = "USER")
    void cadastrarSemPermissao() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.instrutorJson("novo@email.com", "01234567890")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cadastrar: 401 sem autenticação")
    void cadastrarSemLogin() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.instrutorJson("novo@email.com", "01234567890")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listar: 200 com paginação para perfil USER")
    @WithMockUser(roles = "USER")
    void listar() throws Exception {
        repository.save(TestData.instrutor());
        mockMvc.perform(get("/instrutores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").exists());
    }

    @Test
    @DisplayName("Detalhar: 200 para ID existente e 404 para inexistente")
    @WithMockUser(roles = "ADMIN")
    void detalhar() throws Exception {
        Instrutor salvo = repository.save(TestData.instrutor());
        mockMvc.perform(get("/instrutores/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnh").value("01234567890"));
        mockMvc.perform(get("/instrutores/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualizar: 200 alterando o nome")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        Instrutor salvo = repository.save(TestData.instrutor());
        mockMvc.perform(put("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + salvo.getId() + ",\"nome\":\"Nome Alterado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Alterado"));
    }

    @Test
    @DisplayName("Excluir: 204 e instrutor fica inativo")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        Instrutor salvo = repository.save(TestData.instrutor());
        mockMvc.perform(delete("/instrutores/{id}", salvo.getId()))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(salvo.getId()).orElseThrow().isAtivo()).isFalse();
    }
}
