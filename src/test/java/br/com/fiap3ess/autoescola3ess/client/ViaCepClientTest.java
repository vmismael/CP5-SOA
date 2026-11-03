package br.com.fiap3ess.autoescola3ess.client;

import br.com.fiap3ess.autoescola3ess.adapter.out.client.viacep.ViaCepClient;
import br.com.fiap3ess.autoescola3ess.exception.type.CepNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ServicoExternoException;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import br.com.fiap3ess.autoescola3ess.shared.vo.endereco.dto.DadosEndereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Testa o consumo da API externa sem acessar a internet (servidor simulado). */
class ViaCepClientTest {
    private MockRestServiceServer server;
    private ViaCepClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ViaCepClient(builder.build());
    }

    @Test
    @DisplayName("Converte a resposta do ViaCEP em DadosEndereco")
    void consultaComSucesso() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("""
                        {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar",
                         "bairro":"Sé","localidade":"São Paulo","uf":"SP","ibge":"3550308","ddd":"11"}
                        """, MediaType.APPLICATION_JSON));

        DadosEndereco endereco = client.consultarCep("01001-000");

        assertThat(endereco.logradouro()).isEqualTo("Praça da Sé");
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo("SP");
        assertThat(endereco.cep()).isEqualTo("01001-000");
        server.verify();
    }

    @Test
    @DisplayName("Lança CepNotFoundException quando o ViaCEP responde erro=true")
    void cepInexistente() {
        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.consultarCep("99999999"))
                .isInstanceOf(CepNotFoundException.class);
    }

    @Test
    @DisplayName("Lança ValidacaoException para CEP com formato inválido (sem chamar a API)")
    void cepInvalido() {
        assertThatThrownBy(() -> client.consultarCep("123"))
                .isInstanceOf(ValidacaoException.class);
        server.verify();
    }

    @Test
    @DisplayName("Lança ServicoExternoException quando a API externa falha")
    void servicoIndisponivel() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.consultarCep("01001000"))
                .isInstanceOf(ServicoExternoException.class);
    }
}
