package br.com.fiap3ess.autoescola3ess.exception;

import br.com.fiap3ess.autoescola3ess.exception.type.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarGenericNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosBadRequest>> tratarBadRequest(MethodArgumentNotValidException e) {
        List<DadosBadRequest> erros = e.getFieldErrors().stream().map(DadosBadRequest::new).toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler({
            InstrutorNotFoundException.class,
            AlunoNotFoundException.class,
            UsuarioNotFoundException.class,
            InstrucaoNotFoundException.class,
            CepNotFoundException.class})
    public ResponseEntity<DadosErro> tratarNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosErro(e.getMessage()));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<DadosErro> tratarValidacao(ValidacaoException e) {
        return ResponseEntity.badRequest().body(new DadosErro(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosErro> tratarConflito() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new DadosErro("Registro duplicado ou violação de integridade dos dados!"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosErro> tratarAutenticacao() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DadosErro("Login ou senha inválidos!"));
    }

    @ExceptionHandler(ServicoExternoException.class)
    public ResponseEntity<DadosErro> tratarServicoExterno(ServicoExternoException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new DadosErro(e.getMessage()));
    }

    public record DadosBadRequest(String campo, String mensagem) {
        public DadosBadRequest(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    public record DadosErro(String mensagem) {
    }
}
