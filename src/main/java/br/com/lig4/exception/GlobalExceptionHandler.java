package br.com.lig4.exception;

import br.com.lig4.engine.JogadaInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({JogadorNotFoundException.class, PartidaNotFoundException.class,
            SalaNotFoundException.class})
    public ResponseEntity<ErroResponse> naoEncontrado(RuntimeException e) {
        return resposta(HttpStatus.NOT_FOUND, CodigoErro.de(e), e.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> conflito(EmailJaCadastradoException e) {
        return resposta(HttpStatus.CONFLICT, CodigoErro.de(e), e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, CodigoErro.de(e), e.getMessage());
    }

    @ExceptionHandler(PartidaInvalidaException.class)
    public ResponseEntity<ErroResponse> invalida(PartidaInvalidaException e) {
        return resposta(HttpStatus.BAD_REQUEST, CodigoErro.de(e), e.getMessage());
    }

    @ExceptionHandler(SalaInvalidaException.class)
    public ResponseEntity<ErroResponse> salaInvalida(SalaInvalidaException e) {
        HttpStatus status = switch (e.codigo()) {
            case SALA_CHEIA, COR_JA_ESCOLHIDA, SEM_CONVIDADO, FASE_INVALIDA -> HttpStatus.CONFLICT;
            case SEM_PERMISSAO -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return resposta(status, e.codigo(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        List<FieldError> erros = e.getBindingResult().getFieldErrors();
        String mensagem = erros.stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, codigoDaValidacao(erros), mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, CodigoErro.REQUISICAO_INVALIDA,
                "O corpo da requisição está ausente ou é inválido");
    }

    @ExceptionHandler(JogadaInvalidaException.class)
    public ResponseEntity<ErroResponse> jogadaInvalida(JogadaInvalidaException e) {
        HttpStatus status = switch (e.getMotivo()) {
            case JOGO_ENCERRADO, FORA_DO_TURNO -> HttpStatus.CONFLICT;
            case COLUNA_INVALIDA, COLUNA_CHEIA -> HttpStatus.BAD_REQUEST;
        };
        return resposta(status, CodigoErro.de(e), e.getMessage());
    }

    /** Nick, cor, e-mail e senha têm código próprio, porque o front mostra a mensagem junto ao campo. */
    private CodigoErro codigoDaValidacao(List<FieldError> erros) {
        if (erros.stream().anyMatch(erro -> "nick".equals(erro.getField()))) {
            return CodigoErro.NICK_INVALIDO;
        }
        if (erros.stream().anyMatch(erro -> "cor".equals(erro.getField()))) {
            return CodigoErro.COR_INVALIDA;
        }
        if (erros.stream().anyMatch(erro -> "email".equals(erro.getField()))) {
            return CodigoErro.EMAIL_INVALIDO;
        }
        if (erros.stream().anyMatch(erro -> "senha".equals(erro.getField()))) {
            return CodigoErro.SENHA_INVALIDA;
        }
        return CodigoErro.DADOS_INVALIDOS;
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, CodigoErro codigo, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(codigo, mensagem));
    }
}