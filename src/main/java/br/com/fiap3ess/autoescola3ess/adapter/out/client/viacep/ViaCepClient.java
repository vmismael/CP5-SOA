package br.com.fiap3ess.autoescola3ess.adapter.out.client.viacep;

import br.com.fiap3ess.autoescola3ess.exception.type.CepNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ServicoExternoException;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import br.com.fiap3ess.autoescola3ess.shared.vo.endereco.dto.DadosEndereco;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Consome o WebService externo ViaCEP para consultar endereços a partir do CEP. */
@Component
public class ViaCepClient {
    private final RestClient restClient;

    public ViaCepClient(@Qualifier("viaCepRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DadosEndereco consultarCep(String cep) {
        String cepNumerico = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepNumerico.length() != 8) {
            throw new ValidacaoException("CEP inválido! Informe 8 dígitos (ex: 01001-000).");
        }

        ViaCepResponse resposta;
        try {
            resposta = restClient.get()
                    .uri("/{cep}/json/", cepNumerico)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException e) {
            throw new ServicoExternoException("Falha ao consultar o serviço ViaCEP!", e);
        }

        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            throw new CepNotFoundException("CEP não encontrado!");
        }

        String cepFormatado = cepNumerico.substring(0, 5) + "-" + cepNumerico.substring(5);
        return new DadosEndereco(
                resposta.logradouro(),
                null,
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf(),
                cepFormatado
        );
    }
}
