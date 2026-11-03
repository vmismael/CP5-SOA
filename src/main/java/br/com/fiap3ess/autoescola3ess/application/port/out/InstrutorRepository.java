package br.com.fiap3ess.autoescola3ess.application.port.out;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Especialidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {
    Page<Instrutor> findAllByAtivoTrue(Pageable paginacao);

    boolean existsByIdAndAtivoFalse(Long id);

    @Query("""
            select i from Instrutor i
            where i.ativo = true
            and i.especialidade = :especialidade
            and i.id not in (
                select a.instrutor.id from Instrucao a
                where a.dataHora = :dataHora
                and a.motivoCancelamento is null
            )
            """)
    List<Instrutor> buscarDisponiveis(
            @Param("especialidade") Especialidade especialidade,
            @Param("dataHora") LocalDateTime dataHora);

    /**
     * Escolhe aleatoriamente um instrutor ativo, da especialidade informada,
     * que não possua instrução marcada na data/hora. Retorna null se não houver.
     */
    default Instrutor escolherInstrutorAleatorioDisponivel(Especialidade especialidade, LocalDateTime dataHora) {
        List<Instrutor> disponiveis = buscarDisponiveis(especialidade, dataHora);
        if (disponiveis.isEmpty()) {
            return null;
        }
        return disponiveis.get(ThreadLocalRandom.current().nextInt(disponiveis.size()));
    }
}
