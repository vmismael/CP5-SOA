package br.com.fiap3ess.autoescola3ess.adapter.in.controller;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.instrucao.DadosAgendamento;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.response.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3ess.autoescola3ess.application.service.AgendaDeInstrucoes;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.MotivoCancelamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instrucoes")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instruções", description = "Agendamento, consulta e cancelamento de instruções")
public class InstrucaoController {
    @Autowired
    private AgendaDeInstrucoes agenda;

    @PostMapping
    @Operation(summary = "Agenda uma instrução",
            description = "Se o instrutor não for informado, será escolhido um disponível da especialidade informada.")
    public ResponseEntity<DadosDetalhamentoAgendamento> agendarInstrucao(@RequestBody @Valid DadosAgendamento dados) {
        return ResponseEntity.ok(agenda.agendar(dados));
    }

    @GetMapping
    @Operation(summary = "Lista as instruções agendadas (não canceladas)")
    public ResponseEntity<Page<DadosDetalhamentoAgendamento>> listarInstrucoes(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(agenda.listar(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma instrução")
    public ResponseEntity<DadosDetalhamentoAgendamento> detalharInstrucao(@PathVariable Long id) {
        return ResponseEntity.ok(agenda.detalhar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela uma instrução",
            description = "O cancelamento exige motivo e antecedência mínima de 24 horas.")
    public ResponseEntity<Void> cancelarInstrucao(@PathVariable Long id, @RequestParam MotivoCancelamento motivo) {
        agenda.cancelar(id, motivo);
        return ResponseEntity.noContent().build();
    }
}
