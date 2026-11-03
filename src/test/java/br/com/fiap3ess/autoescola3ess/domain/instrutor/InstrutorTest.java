package br.com.fiap3ess.autoescola3ess.domain.instrutor;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrutor.DadosAtualizacaoInstrutor;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Especialidade;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrutorTest {
    @Test
    @DisplayName("Atualiza apenas os campos informados")
    void atualizarInformacoes() {
        Instrutor instrutor = TestData.instrutor();
        instrutor.atualizarInformacoes(new DadosAtualizacaoInstrutor(1L, "Novo Nome", null, "", Especialidade.CARROS, null));

        assertThat(instrutor.getNome()).isEqualTo("Novo Nome");
        assertThat(instrutor.getEspecialidade()).isEqualTo(Especialidade.CARROS);
        assertThat(instrutor.getEmail()).isEqualTo("instrutorteste@email.com.br");
        assertThat(instrutor.getTelefone()).isEqualTo("(11) 91234-5678");
    }

    @Test
    @DisplayName("Excluir inativa o instrutor (exclusão lógica)")
    void excluir() {
        Instrutor instrutor = TestData.instrutor();
        instrutor.excluir();
        assertThat(instrutor.isAtivo()).isFalse();
    }
}
