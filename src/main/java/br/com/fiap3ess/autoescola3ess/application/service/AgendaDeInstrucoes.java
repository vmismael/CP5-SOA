package br.com.fiap3ess.autoescola3ess.application.service;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.application.core.specification.contract.ValidadorAgendamento;
import br.com.fiap3ess.autoescola3ess.application.port.out.AlunoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrucaoRepository;
import br.com.fiap3ess.autoescola3ess.application.port.out.InstrutorRepository;
import br.com.fiap3ess.autoescola3ess.exception.type.AlunoNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.InstrucaoNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.InstrutorNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.MotivoCancelamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendaDeInstrucoes {
    private static final long ANTECEDENCIA_MINIMA_CANCELAMENTO_HORAS = 24;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private InstrutorRepository instrutorRepository;

    @Autowired
    private InstrucaoRepository repository;

    @Autowired
    private List<ValidadorAgendamento> validadoresAgendamento;

    @Transactional
    public DadosDetalhamentoAgendamento agendar(DadosAgendamento dados) {
        if (!alunoRepository.existsById(dados.idAluno())) {
            throw new AlunoNotFoundException("ID do aluno informado não existe!");
        }
        if (dados.idInstrutor() != null && !instrutorRepository.existsById(dados.idInstrutor())) {
            throw new InstrutorNotFoundException("ID do instrutor informado não existe!");
        }
        //Validações
        validadoresAgendamento.forEach(validador -> validador.validar(dados));

        Aluno aluno = alunoRepository.getReferenceById(dados.idAluno());

        Instrutor instrutor = escolherInstrutor(dados);
        if (instrutor == null) {
            throw new ValidacaoException("Nenhum instrutor disponível para a data/hora informada!");
        }

        Instrucao instrucao = new Instrucao(null, aluno, instrutor, dados.dataHora());
        Instrucao salva = repository.save(instrucao);
        return new DadosDetalhamentoAgendamento(salva);
    }

    @Transactional
    public void cancelar(Long idInstrucao, MotivoCancelamento motivo) {
        Instrucao instrucao = repository.findById(idInstrucao)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));

        if (instrucao.isCancelada()) {
            throw new ValidacaoException("Instrução já está cancelada!");
        }
        long horasDeAntecedencia = Duration.between(LocalDateTime.now(), instrucao.getDataHora()).toHours();
        if (horasDeAntecedencia < ANTECEDENCIA_MINIMA_CANCELAMENTO_HORAS) {
            throw new ValidacaoException(
                    "Instrução somente pode ser cancelada com antecedência mínima de 24 horas!");
        }
        instrucao.cancelar(motivo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoAgendamento> listar(Pageable paginacao) {
        return repository.findAllByMotivoCancelamentoIsNull(paginacao).map(DadosDetalhamentoAgendamento::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAgendamento detalhar(Long id) {
        return repository.findById(id)
                .map(DadosDetalhamentoAgendamento::new)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));
    }

    private Instrutor escolherInstrutor(DadosAgendamento dados) {
        if (dados.idInstrutor() != null) {
            return instrutorRepository.getReferenceById(dados.idInstrutor());
        }
        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é campo obrigatório, caso o instrutor não seja informado!");
        }
        return instrutorRepository.escolherInstrutorAleatorioDisponivel(
                dados.especialidade(),
                dados.dataHora()
        );
    }
}
