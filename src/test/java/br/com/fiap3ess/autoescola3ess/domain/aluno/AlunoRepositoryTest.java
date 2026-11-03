package br.com.fiap3ess.autoescola3ess.domain.aluno;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Aluno;
import br.com.fiap3ess.autoescola3ess.application.port.out.AlunoRepository;
import br.com.fiap3ess.autoescola3ess.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AlunoRepositoryTest {
    @Autowired
    AlunoRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("Persiste e recupera o aluno com o endereço embutido")
    void salvarERecuperar() {
        Aluno salvo = repository.save(TestData.aluno());
        em.flush();
        em.clear();

        Aluno encontrado = repository.findById(salvo.getId()).orElseThrow();
        assertThat(encontrado.getNome()).isEqualTo("Aluno Teste");
        assertThat(encontrado.getEndereco().getCidade()).isEqualTo("Test City");
    }

    @Test
    @DisplayName("findAllByAtivoTrue ignora alunos inativos")
    void listarSomenteAtivos() {
        Aluno ativo = repository.save(TestData.aluno());
        Aluno inativo = repository.save(TestData.aluno());
        inativo.excluir();
        em.flush();

        var pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 10));
        assertThat(pagina.getContent()).extracting(Aluno::getId).contains(ativo.getId()).doesNotContain(inativo.getId());
    }

    @Test
    @DisplayName("existsByIdAndAtivoFalse identifica aluno inativo")
    void existeInativo() {
        Aluno inativo = repository.save(TestData.aluno());
        inativo.excluir();
        Aluno ativo = repository.save(TestData.aluno());
        em.flush();

        assertThat(repository.existsByIdAndAtivoFalse(inativo.getId())).isTrue();
        assertThat(repository.existsByIdAndAtivoFalse(ativo.getId())).isFalse();
    }
}
