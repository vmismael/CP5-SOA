package br.com.fiap3ess.autoescola3ess.adapter.in.controller.response.instrutor;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Especialidade;

public record DadosListagemInstrutor(
        Long id,
        String nome,
        String email,
        Especialidade especialidade) {
    public DadosListagemInstrutor(Instrutor instrutor) {
        this(
                instrutor.getId(),
                instrutor.getNome(),
                instrutor.getEmail(),
                instrutor.getEspecialidade()
        );
    }
}
