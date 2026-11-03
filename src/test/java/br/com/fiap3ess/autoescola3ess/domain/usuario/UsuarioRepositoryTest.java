package br.com.fiap3ess.autoescola3ess.domain.usuario;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Usuario;
import br.com.fiap3ess.autoescola3ess.application.port.out.UsuarioRepository;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Role;
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
class UsuarioRepositoryTest {
    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("findByLogin retorna o usuário com seu perfil")
    void findByLogin() {
        repository.save(new Usuario("maria", "hash", Role.ADMIN));
        em.flush();

        var usuario = repository.findByLogin("maria");
        assertThat(usuario).isNotNull();
        assertThat(usuario.getUsername()).isEqualTo("maria");
        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("findByLogin retorna null para login inexistente")
    void findByLoginInexistente() {
        assertThat(repository.findByLogin("ninguem")).isNull();
    }

    @Test
    @DisplayName("existsByLogin informa se o login já está cadastrado")
    void existsByLogin() {
        repository.save(new Usuario("joao", "hash", Role.USER));
        em.flush();

        assertThat(repository.existsByLogin("joao")).isTrue();
        assertThat(repository.existsByLogin("outro")).isFalse();
    }
}
