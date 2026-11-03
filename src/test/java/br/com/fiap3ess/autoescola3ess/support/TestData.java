package br.com.fiap3ess.autoescola3ess.support;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Instrutor;
import br.com.fiap3ess.autoescola3ess.shared.vo.endereco.Endereco;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Especialidade;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/** Dados e JSONs reutilizados pelos testes. */
public final class TestData {
    public static final DateTimeFormatter FORMATO_JSON = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    public static final String ENDERECO_JSON = """
            {"logradouro":"Rua Teste","numero":"10","complemento":"apto 1","bairro":"Centro",            "cidade":"Sao Paulo","uf":"SP","cep":"01001-000"}""";

    private TestData() {
    }

    public static Endereco endereco() {
        return new Endereco("Rua Teste", "000", "apto. 00", "Vila Teste", "Test City", "TS", "01234-567");
    }

    public static Aluno aluno() {
        return new Aluno(null, "Aluno Teste", "alunoteste@email.com.br", "(11) 98765-4321",
                "12345678901", endereco(), true);
    }

    public static Instrutor instrutor() {
        return new Instrutor(null, "Instrutor Teste", "instrutorteste@email.com.br", "(11) 91234-5678",
                "01234567890", Especialidade.MOTOS, endereco(), true);
    }

    public static LocalDateTime proximo(DayOfWeek dia, int hora) {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(dia))
                .withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }

    public static String instrutorJson(String email, String cnh) {
        return """
                {"nome":"Instrutor Novo","email":"%s","telefone":"(11) 91234-5678","cnh":"%s",                "especialidade":"CARROS","endereco":%s}""".formatted(email, cnh, ENDERECO_JSON);
    }

    public static String alunoJson(String email, String cpf) {
        return """
                {"nome":"Aluno Novo","email":"%s","telefone":"(11) 98765-4321","cpf":"%s","endereco":%s}"""
                .formatted(email, cpf, ENDERECO_JSON);
    }
}
