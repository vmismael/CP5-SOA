package br.com.fiap3ess.autoescola3ess.domain.instrucao;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrucaoRepository;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.MotivoCancelamento;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InstrucaoRepositoryTest {
    @Autowired
    InstrucaoRepository repository;

    @Autowired
    TestEntityManager em;

    private Instrucao agendar(LocalDateTime dataHora) {
        Aluno aluno = em.persist(TestData.aluno());
        Instrutor instrutor = em.persist(TestData.instrutor());
        return em.persist(new Instrucao(null, aluno, instrutor, dataHora));
    }

    @Test
    @DisplayName("Detecta instrutor ocupado na data/hora")
    void instrutorOcupado() {
        LocalDateTime dataHora = TestData.proximo(DayOfWeek.MONDAY, 10);
        Instrucao instrucao = agendar(dataHora);
        em.flush();

        Long idInstrutor = instrucao.getInstrutor().getId();
        assertThat(repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(idInstrutor, dataHora)).isTrue();
        assertThat(repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(idInstrutor, dataHora.plusHours(1))).isFalse();
    }

    @Test
    @DisplayName("Instrução cancelada não bloqueia o horário do instrutor")
    void instrucaoCanceladaLiberaHorario() {
        LocalDateTime dataHora = TestData.proximo(DayOfWeek.MONDAY, 10);
        Instrucao instrucao = agendar(dataHora);
        instrucao.cancelar(MotivoCancelamento.OUTROS);
        em.flush();

        Long idInstrutor = instrucao.getInstrutor().getId();
        assertThat(repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(idInstrutor, dataHora)).isFalse();
    }

    @Test
    @DisplayName("Detecta outra instrução do mesmo aluno no mesmo dia")
    void limiteDiarioAluno() {
        LocalDateTime dataHora = TestData.proximo(DayOfWeek.TUESDAY, 9);
        Instrucao instrucao = agendar(dataHora);
        em.flush();

        Long idAluno = instrucao.getAluno().getId();
        assertThat(repository.existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                idAluno, dataHora.toLocalDate().atStartOfDay(), dataHora.toLocalDate().atTime(23, 59))).isTrue();
        assertThat(repository.existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                idAluno, dataHora.plusDays(1).toLocalDate().atStartOfDay(), dataHora.plusDays(1).toLocalDate().atTime(23, 59))).isFalse();
    }

    @Test
    @DisplayName("Listagem exclui instruções canceladas")
    void listagemSemCanceladas() {
        Instrucao ativa = agendar(TestData.proximo(DayOfWeek.MONDAY, 10));
        Instrucao cancelada = agendar(TestData.proximo(DayOfWeek.MONDAY, 11));
        cancelada.cancelar(MotivoCancelamento.ALUNO_DESISTIU);
        em.flush();

        var pagina = repository.findAllByMotivoCancelamentoIsNull(PageRequest.of(0, 10));
        assertThat(pagina.getContent()).extracting(Instrucao::getId)
                .contains(ativa.getId()).doesNotContain(cancelada.getId());
    }
}
