package br.com.fiap3ess.autoescola3ess.service;

import br.com.fiap3ess.autoescola3ess.application.core.specification.ValidadorHoraInteira;
import br.com.fiap3ess.autoescola3ess.application.core.specification.ValidadorHorarioAntecedencia;
import br.com.fiap3ess.autoescola3ess.application.core.specification.ValidadorHorarioFuncionamento;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadoresAgendamentoTest {
    private DadosAgendamento dados(LocalDateTime dataHora) {
        return new DadosAgendamento(1L, 1L, null, dataHora);
    }

    @Test
    @DisplayName("Horário de funcionamento: aceita segunda 10h, rejeita domingo e fora do expediente")
    void horarioFuncionamento() {
        var validador = new ValidadorHorarioFuncionamento();
        assertThatCode(() -> validador.validar(dados(TestData.proximo(DayOfWeek.MONDAY, 10)))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validador.validar(dados(TestData.proximo(DayOfWeek.SUNDAY, 10))))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> validador.validar(dados(TestData.proximo(DayOfWeek.MONDAY, 5))))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> validador.validar(dados(TestData.proximo(DayOfWeek.MONDAY, 21))))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Hora inteira: rejeita minutos diferentes de zero")
    void horaInteira() {
        var validador = new ValidadorHoraInteira();
        assertThatCode(() -> validador.validar(dados(TestData.proximo(DayOfWeek.MONDAY, 10)))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validador.validar(dados(TestData.proximo(DayOfWeek.MONDAY, 10).withMinute(30))))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Antecedência: exige no mínimo 30 minutos")
    void antecedencia() {
        var validador = new ValidadorHorarioAntecedencia();
        assertThatCode(() -> validador.validar(dados(LocalDateTime.now().plusHours(2)))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validador.validar(dados(LocalDateTime.now().plusMinutes(10))))
                .isInstanceOf(ValidacaoException.class);
    }
}
