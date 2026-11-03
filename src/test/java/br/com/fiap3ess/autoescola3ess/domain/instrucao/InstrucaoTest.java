package br.com.fiap3ess.autoescola3ess.domain.instrucao;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.MotivoCancelamento;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InstrucaoTest {
    @Test
    @DisplayName("Instrução nasce ativa e passa a cancelada ao informar o motivo")
    void cancelar() {
        Instrucao instrucao = new Instrucao(null, TestData.aluno(), TestData.instrutor(), LocalDateTime.now().plusDays(2));
        assertThat(instrucao.isCancelada()).isFalse();

        instrucao.cancelar(MotivoCancelamento.INSTRUTOR_CANCELOU);

        assertThat(instrucao.isCancelada()).isTrue();
        assertThat(instrucao.getMotivoCancelamento()).isEqualTo(MotivoCancelamento.INSTRUTOR_CANCELOU);
    }
}
