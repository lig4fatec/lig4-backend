package br.com.lig4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import br.com.lig4.engine.JogadaInvalidaException;

import java.util.stream.Collectors;

/** Traduz exceções em respostas HTTP (substitui os ExceptionMappers do Quarkus). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({JogadorNotFoundException.class, PartidaNotFoundException.class})
    public ResponseEntity<ErroResponse> naoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> conflito(EmailJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(PartidaInvalidaException.class)
    public ResponseEntity<ErroResponse> invalida(PartidaInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse(mensagem));
    }

    @ExceptionHandler(JogadaInvalidaException.class)
    public ResponseEntity<ErroResponse> jogadaInvalida(JogadaInvalidaException e) {
        HttpStatus status = switch (e.getMotivo()) {
            case JOGO_ENCERRADO, FORA_DO_TURNO -> HttpStatus.CONFLICT;
            case COLUNA_INVALIDA, COLUNA_CHEIA -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new ErroResponse(e.getMessage()));
    }
}
