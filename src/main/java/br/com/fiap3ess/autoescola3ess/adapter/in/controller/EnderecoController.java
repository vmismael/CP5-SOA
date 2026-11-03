package br.com.fiap3ess.autoescola3ess.adapter.in.controller;

import br.com.fiap3ess.autoescola3ess.adapter.out.client.viacep.ViaCepClient;
import br.com.fiap3ess.autoescola3ess.shared.vo.endereco.dto.DadosEndereco;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Endereços", description = "Consulta de endereço por CEP via API externa (ViaCEP)")
@RequiredArgsConstructor
public class EnderecoController {
    private final ViaCepClient viaCepClient;

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consulta um endereço pelo CEP",
            description = "Consome o WebService externo ViaCEP (https://viacep.com.br).")
    public ResponseEntity<DadosEndereco> consultarCep(@PathVariable String cep) {
        return ResponseEntity.ok(viaCepClient.consultarCep(cep));
    }
}
