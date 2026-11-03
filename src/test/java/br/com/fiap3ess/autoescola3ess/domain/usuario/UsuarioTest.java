package br.com.fiap3ess.autoescola3ess.domain.usuario;

import br.com.fiap3ess.autoescola3ess.application.core.domain.Usuario;
import br.com.fiap3ess.autoescola3ess.shared.vo.enumeration.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {
    @Test
    @DisplayName("Expõe login, senha e autoridade conforme o perfil")
    void userDetails() {
        Usuario usuario = new Usuario("ana", "hash", Role.USER);

        assertThat(usuario.getUsername()).isEqualTo("ana");
        assertThat(usuario.getPassword()).isEqualTo("hash");
        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(usuario.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Atualiza senha e perfil somente quando informados")
    void atualizarInformacoes() {
        Usuario usuario = new Usuario("ana", "hash", Role.USER);

        usuario.atualizarInformacoes(null, Role.ADMIN);
        assertThat(usuario.getPerfil()).isEqualTo(Role.ADMIN);
        assertThat(usuario.getPassword()).isEqualTo("hash");

        usuario.atualizarInformacoes("novoHash", null);
        assertThat(usuario.getPassword()).isEqualTo("novoHash");
        assertThat(usuario.getPerfil()).isEqualTo(Role.ADMIN);
    }
}
