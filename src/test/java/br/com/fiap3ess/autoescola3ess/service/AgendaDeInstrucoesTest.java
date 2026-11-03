package br.com.fiap3ess.autoescola3ess.service;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.application.port.out.AlunoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrucaoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrutorRepository;
import br.com.fiap3ess.autoescola3ess.application.service.AgendaDeInstrucoes;
import br.com.fiap3ess.autoescola3ess.exception.type.AlunoNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.InstrucaoNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Especialidade;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.MotivoCancelamento;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaDeInstrucoesTest {
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstrutorRepository instrutorRepository;
    @Mock
    InstrucaoRepository instrucaoRepository;

    AgendaDeInstrucoes agenda;

    @BeforeEach
    void setUp() {
        agenda = new AgendaDeInstrucoes();
        ReflectionTestUtils.setField(agenda, "alunoRepository", alunoRepository);
        ReflectionTestUtils.setField(agenda, "instrutorRepository", instrutorRepository);
        ReflectionTestUtils.setField(agenda, "repository", instrucaoRepository);
        ReflectionTestUtils.setField(agenda, "validadoresAgendamento", List.of());
    }

    @Test
    @DisplayName("Agendar com aluno inexistente lança AlunoNotFoundException")
    void alunoInexistente() {
        when(alunoRepository.existsById(1L)).thenReturn(false);
        var dados = new DadosAgendamento(1L, null, Especialidade.MOTOS, TestData.proximo(DayOfWeek.MONDAY, 10));

        assertThatThrownBy(() -> agenda.agendar(dados)).isInstanceOf(AlunoNotFoundException.class);
    }

    @Test
    @DisplayName("Agendar sem instrutor disponível lança ValidacaoException")
    void semInstrutorDisponivel() {
        var dataHora = TestData.proximo(DayOfWeek.MONDAY, 10);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(TestData.aluno());
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, dataHora)).thenReturn(null);

        assertThatThrownBy(() -> agenda.agendar(new DadosAgendamento(1L, null, Especialidade.MOTOS, dataHora)))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("Nenhum instrutor disponível");
    }

    @Test
    @DisplayName("Agendar sem instrutor e sem especialidade lança ValidacaoException")
    void semEspecialidade() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(TestData.aluno());

        assertThatThrownBy(() -> agenda.agendar(new DadosAgendamento(1L, null, null, TestData.proximo(DayOfWeek.MONDAY, 10))))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Agendar escolhe instrutor disponível e salva a instrução")
    void agendarComSucesso() {
        var dataHora = TestData.proximo(DayOfWeek.MONDAY, 10);
        Aluno aluno = TestData.aluno();
        Instrutor instrutor = TestData.instrutor();
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, dataHora)).thenReturn(instrutor);
        when(instrucaoRepository.save(any(Instrucao.class))).thenAnswer(i -> i.getArgument(0));

        DadosDetalhamentoAgendamento resultado =
                agenda.agendar(new DadosAgendamento(1L, null, Especialidade.MOTOS, dataHora));

        assertThat(resultado.nomeAluno()).isEqualTo("Aluno Teste");
        assertThat(resultado.nomeInstrutor()).isEqualTo("Instrutor Teste");
        assertThat(resultado.dataHora()).isEqualTo(dataHora);
    }

    @Test
    @DisplayName("Cancelar com 24h ou mais de antecedência marca a instrução como cancelada")
    void cancelarComSucesso() {
        Instrucao instrucao = new Instrucao(1L, TestData.aluno(), TestData.instrutor(), LocalDateTime.now().plusDays(2));
        when(instrucaoRepository.findById(1L)).thenReturn(Optional.of(instrucao));

        agenda.cancelar(1L, MotivoCancelamento.ALUNO_DESISTIU);

        assertThat(instrucao.isCancelada()).isTrue();
    }

    @Test
    @DisplayName("Cancelar com menos de 24h de antecedência lança ValidacaoException")
    void cancelarSemAntecedencia() {
        Instrucao instrucao = new Instrucao(1L, TestData.aluno(), TestData.instrutor(), LocalDateTime.now().plusHours(3));
        when(instrucaoRepository.findById(1L)).thenReturn(Optional.of(instrucao));

        assertThatThrownBy(() -> agenda.cancelar(1L, MotivoCancelamento.OUTROS)).isInstanceOf(ValidacaoException.class);
        assertThat(instrucao.isCancelada()).isFalse();
    }

    @Test
    @DisplayName("Cancelar instrução inexistente lança InstrucaoNotFoundException")
    void cancelarInexistente() {
        when(instrucaoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agenda.cancelar(9L, MotivoCancelamento.OUTROS))
                .isInstanceOf(InstrucaoNotFoundException.class);
    }
}
