package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.port.out.AlunoRepository;
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
class AlunoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AlunoRepository repository;

    @Test
    @DisplayName("Cadastrar: 201 com Location para dados válidos")
    @WithMockUser(roles = "ADMIN")
    void cadastrar() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.alunoJson("aluno@email.com", "98765432100")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.cpf").value("98765432100"));
    }

    @Test
    @DisplayName("Cadastrar: 400 para CPF inválido")
    @WithMockUser(roles = "ADMIN")
    void cadastrarCpfInvalido() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.alunoJson("aluno@email.com", "123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar: 200 com paginação")
    @WithMockUser(roles = "USER")
    void listar() throws Exception {
        repository.save(TestData.aluno());
        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").exists());
    }

    @Test
    @DisplayName("Detalhar: 200 para ID existente e 404 para inexistente")
    @WithMockUser(roles = "ADMIN")
    void detalhar() throws Exception {
        Aluno salvo = repository.save(TestData.aluno());
        mockMvc.perform(get("/alunos/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alunoteste@email.com.br"));
        mockMvc.perform(get("/alunos/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualizar: 200 alterando o telefone")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        Aluno salvo = repository.save(TestData.aluno());
        mockMvc.perform(put("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + salvo.getId() + ",\"telefone\":\"(11) 90000-0000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("(11) 90000-0000"));
    }

    @Test
    @DisplayName("Excluir: 204 e aluno fica inativo")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        Aluno salvo = repository.save(TestData.aluno());
        mockMvc.perform(delete("/alunos/{id}", salvo.getId()))
                .andExpect(status().isNoContent());
        assertThat(repository.findById(salvo.getId()).orElseThrow().isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Cadastrar: 403 para perfil USER")
    @WithMockUser(roles = "USER")
    void cadastrarSemPermissao() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.alunoJson("aluno@email.com", "98765432100")))
                .andExpect(status().isForbidden());
    }
}
