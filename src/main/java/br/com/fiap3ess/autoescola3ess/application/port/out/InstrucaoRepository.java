package br.com.fiap3ess.autoescola3ess.application.port.out;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrucao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    Page<Instrucao> findAllByMotivoCancelamentoIsNull(Pageable paginacao);

    boolean existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(Long idInstrutor, LocalDateTime dataHora);

    boolean existsByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
            Long idAluno, LocalDateTime inicioDoDia, LocalDateTime fimDoDia);
}
