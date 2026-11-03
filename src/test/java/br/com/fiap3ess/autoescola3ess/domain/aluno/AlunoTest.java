package br.com.fiap3ess.autoescola3ess.domain.aluno;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.aluno.DadosAtualizacaoAluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlunoTest {
    @Test
    @DisplayName("Atualiza apenas os campos informados")
    void atualizarInformacoes() {
        Aluno aluno = TestData.aluno();
        aluno.atualizarInformacoes(new DadosAtualizacaoAluno(1L, null, "novo@email.com", null, null));

        assertThat(aluno.getEmail()).isEqualTo("novo@email.com");
        assertThat(aluno.getNome()).isEqualTo("Aluno Teste");
        assertThat(aluno.getCpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Excluir inativa o aluno (exclusão lógica)")
    void excluir() {
        Aluno aluno = TestData.aluno();
        aluno.excluir();
        assertThat(aluno.isAtivo()).isFalse();
    }
}
