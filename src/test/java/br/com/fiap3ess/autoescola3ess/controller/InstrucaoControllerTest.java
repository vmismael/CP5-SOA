package br.com.fiap3ess.autoescola3ess.controller;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.application.port.out.AlunoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrucaoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrutorRepository;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
@WithMockUser
public class InstrucaoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AlunoRepository alunoRepository;

    @Autowired
    InstrutorRepository instrutorRepository;

    @Autowired
    InstrucaoRepository instrucaoRepository;

    private String corpo(Long idAluno, Long idInstrutor, LocalDateTime dataHora) {
        return "{\"id_aluno\":" + idAluno + ",\"id_instrutor\":" + idInstrutor
                + ",\"data_hora\":\"" + dataHora.format(TestData.FORMATO_JSON) + "\"}";
    }

    @Test
    @DisplayName("Expectativa: Retornar código 400 para dados inválidos.")
    void agendarInstrucaoCenario1() throws Exception {
        mockMvc.perform(post("/instrucoes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: Retornar 200 e detalhes ao agendar com dados válidos.")
    void agendarInstrucaoCenario2() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), instrutor.getId(), TestData.proximo(DayOfWeek.MONDAY, 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome_aluno").value("Aluno Teste"))
                .andExpect(jsonPath("$.nome_instrutor").value("Instrutor Teste"));
    }

    @Test
    @DisplayName("Expectativa: Retornar 400 ao agendar em domingo.")
    void agendarInstrucaoCenario3() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), instrutor.getId(), TestData.proximo(DayOfWeek.SUNDAY, 10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Tentativa de agendamento fora do horário de funcionamento!"));
    }

    @Test
    @DisplayName("Expectativa: Retornar 404 ao agendar para aluno inexistente.")
    void agendarInstrucaoCenario4() throws Exception {
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(999999L, instrutor.getId(), TestData.proximo(DayOfWeek.MONDAY, 10))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Expectativa: Retornar 400 ao agendar com instrutor ocupado.")
    void agendarInstrucaoCenario5() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Aluno outro = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());
        LocalDateTime dataHora = TestData.proximo(DayOfWeek.MONDAY, 10);
        instrucaoRepository.save(new Instrucao(null, outro, instrutor, dataHora));

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(aluno.getId(), instrutor.getId(), dataHora)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Instrutor ocupado na data/hora escolhida!"));
    }

    @Test
    @DisplayName("Expectativa: Listar e detalhar instruções.")
    void consultarInstrucoes() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());
        Instrucao instrucao = instrucaoRepository.save(
                new Instrucao(null, aluno, instrutor, TestData.proximo(DayOfWeek.MONDAY, 10)));

        mockMvc.perform(get("/instrucoes")).andExpect(status().isOk());
        mockMvc.perform(get("/instrucoes/{id}", instrucao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome_aluno").value("Aluno Teste"));
        mockMvc.perform(get("/instrucoes/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Expectativa: Cancelar com antecedência retorna 204 e marca como cancelada.")
    void cancelarInstrucaoCenario1() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());
        Instrucao instrucao = instrucaoRepository.save(
                new Instrucao(null, aluno, instrutor, LocalDateTime.now().plusDays(3)));

        mockMvc.perform(delete("/instrucoes/{id}", instrucao.getId()).param("motivo", "ALUNO_DESISTIU"))
                .andExpect(status().isNoContent());
        assertThat(instrucaoRepository.findById(instrucao.getId()).orElseThrow().isCancelada()).isTrue();

        mockMvc.perform(delete("/instrucoes/{id}", instrucao.getId()).param("motivo", "OUTROS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: Cancelar com menos de 24h de antecedência retorna 400.")
    void cancelarInstrucaoCenario2() throws Exception {
        Aluno aluno = alunoRepository.save(TestData.aluno());
        Instrutor instrutor = instrutorRepository.save(TestData.instrutor());
        Instrucao instrucao = instrucaoRepository.save(
                new Instrucao(null, aluno, instrutor, LocalDateTime.now().plusHours(2)));

        mockMvc.perform(delete("/instrucoes/{id}", instrucao.getId()).param("motivo", "OUTROS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Expectativa: Cancelar sem motivo retorna 400 e ID inexistente retorna 404.")
    void cancelarInstrucaoCenario3() throws Exception {
        mockMvc.perform(delete("/instrucoes/{id}", 1)).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/instrucoes/{id}", 999999).param("motivo", "OUTROS"))
                .andExpect(status().isNotFound());
    }
}
