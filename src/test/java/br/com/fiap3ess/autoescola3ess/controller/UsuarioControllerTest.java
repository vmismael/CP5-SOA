package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Usuario;
import br.com.fiap3ess.autoescola3ess.application.port.out.UsuarioRepository;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Role;
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
class UsuarioControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private Usuario salvarUsuario(String login) {
        return repository.save(new Usuario(login, passwordEncoder.encode("senha123"), Role.USER));
    }

    @Test
    @DisplayName("Cadastrar: 201 e a senha não é retornada")
    @WithMockUser(roles = "ADMIN")
    void cadastrar() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"novo.usuario\",\"senha\":\"senha123\",\"perfil\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("novo.usuario"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("Cadastrar: 400 para login duplicado")
    @WithMockUser(roles = "ADMIN")
    void cadastrarLoginDuplicado() throws Exception {
        salvarUsuario("duplicado");
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"duplicado\",\"senha\":\"senha123\",\"perfil\":\"USER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cadastrar: 400 para dados inválidos")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInvalido() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Listar e detalhar: 200 / 404 para inexistente")
    @WithMockUser(roles = "ADMIN")
    void listarEDetalhar() throws Exception {
        Usuario usuario = salvarUsuario("consulta");
        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/usuarios/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("consulta"));
        mockMvc.perform(get("/usuarios/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualizar: 200 alterando o perfil")
    @WithMockUser(roles = "ADMIN")
    void atualizar() throws Exception {
        Usuario usuario = salvarUsuario("atualiza");
        mockMvc.perform(put("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + usuario.getId() + ",\"perfil\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    @DisplayName("Excluir: 204 e usuário removido")
    @WithMockUser(roles = "ADMIN")
    void excluir() throws Exception {
        Usuario usuario = salvarUsuario("exclui");
        mockMvc.perform(delete("/usuarios/{id}", usuario.getId())).andExpect(status().isNoContent());
        assertThat(repository.findById(usuario.getId())).isEmpty();
    }

    @Test
    @DisplayName("Listar: 403 para perfil USER")
    @WithMockUser(roles = "USER")
    void listarSemPermissao() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }
}
