package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.adapter.out.client.viacep.ViaCepClient;
import br.com.fiap3ess.autoescola3ess.exception.type.CepNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ServicoExternoException;
import br.com.fiap3ess.autoescola3ess.shared.vo.endereco.dto.DadosEndereco;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.when;
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
@WithMockUser
class EnderecoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ViaCepClient viaCepClient;

    @Test
    @DisplayName("200 com o endereço retornado pelo serviço externo")
    void consultarCep() throws Exception {
        when(viaCepClient.consultarCep("01001000")).thenReturn(new DadosEndereco(
                "Praça da Sé", null, "lado ímpar", "Sé", "São Paulo", "SP", "01001-000"));

        mockMvc.perform(get("/enderecos/cep/{cep}", "01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.cep").value("01001-000"));
    }

    @Test
    @DisplayName("404 quando o CEP não existe")
    void cepNaoEncontrado() throws Exception {
        when(viaCepClient.consultarCep("99999999")).thenThrow(new CepNotFoundException("CEP não encontrado!"));

        mockMvc.perform(get("/enderecos/cep/{cep}", "99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("502 quando o serviço externo está indisponível")
    void servicoIndisponivel() throws Exception {
        when(viaCepClient.consultarCep("01001000"))
                .thenThrow(new ServicoExternoException("Falha ao consultar o serviço ViaCEP!", new RuntimeException()));

        mockMvc.perform(get("/enderecos/cep/{cep}", "01001000"))
                .andExpect(status().isBadGateway());
    }
}
