package br.com.fiap3ess.autoescola3ess.adapter.out.client.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Resposta da API pública ViaCEP (https://viacep.com.br). Quando o CEP não existe, vem apenas {"erro": true}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro) {
}
