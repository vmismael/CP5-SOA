package br.com.fiap3ess.autoescola3ess.application.service;

import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.usuario.DadosAtualizacaoUsuario;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.request.usuario.DadosCadastroUsuario;
import br.com.fiap3ess.autoescola3ess.adapter.in.controller.response.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3ess.autoescola3ess.application.core.domain.Usuario;
import br.com.fiap3ess.autoescola3ess.application.port.out.UsuarioRepository;
import br.com.fiap3ess.autoescola3ess.exception.type.UsuarioNotFoundException;
import br.com.fiap3ess.autoescola3ess.exception.type.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new ValidacaoException("Já existe um usuário com o login informado!");
        }
        Usuario usuario = new Usuario(dados.login(), passwordEncoder.encode(dados.senha()), dados.perfil());
        return new DadosDetalhamentoUsuario(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listarUsuarios(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarUsuario(DadosAtualizacaoUsuario dados) {
        Usuario usuario = buscar(dados.id());
        String novaSenha = dados.senha() != null ? passwordEncoder.encode(dados.senha()) : null;
        usuario.atualizarInformacoes(novaSenha, dados.perfil());
        return new DadosDetalhamentoUsuario(repository.save(usuario));
    }

    @Transactional
    public void excluirUsuario(Long id) {
        repository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("ID do usuário informado não existe!"));
    }
}
