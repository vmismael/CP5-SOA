package br.com.fiap3ess.autoescola3ess.application.core.specification;

import br.com.fiap3ess.autoescola3ess.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrucaoRepository;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorLimiteDiarioAluno implements ValidadorAgendamento {
    @Autowired
    private InstrucaoRepository repository;

    @Override
    public void validar(DadosAgendamento dados) {
        LocalDateTime inicioDoDia = dados.dataHora().toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = dados.dataHora().toLocalDate().atTime(23, 59, 59);

        boolean reincidencia = repository.existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                dados.idAluno(),
                inicioDoDia,
                fimDoDia
        );

        if (reincidencia) {
            throw new ValidacaoException("Aluno já possui uma instrução agendada neste dia!");
        }
    }
}
