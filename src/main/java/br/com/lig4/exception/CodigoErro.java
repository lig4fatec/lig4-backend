package br.com.lig4.exception;

import br.com.lig4.engine.JogadaInvalidaException;

/** Códigos estáveis de erro. O front-end decide o que mostrar a partir deles, sem ler a mensagem. */
public enum CodigoErro {
    JOGADOR_NAO_ENCONTRADO,
    PARTIDA_NAO_ENCONTRADA,
    EMAIL_JA_CADASTRADO,
    PARTIDA_INVALIDA,
    DADOS_INVALIDOS,
    EMAIL_INVALIDO,
    COR_INVALIDA,
    REQUISICAO_INVALIDA,
    ERRO_INTERNO,
    // salas
    PIN_INVALIDO,
    PIN_NAO_ENCONTRADO,
    SALA_CHEIA,
    COR_JA_ESCOLHIDA,
    SERIE_INVALIDA,
    SEM_PERMISSAO,
    SEM_CONVIDADO,
    FASE_INVALIDA,
    // motivos de jogada inválida (mesmos nomes de JogadaInvalidaException.Motivo)
    JOGO_ENCERRADO,
    FORA_DO_TURNO,
    COLUNA_INVALIDA,
    COLUNA_CHEIA;
    

    public static CodigoErro de(RuntimeException e) {
        if (e instanceof JogadorNotFoundException) {
            return JOGADOR_NAO_ENCONTRADO;
        }
        if (e instanceof PartidaNotFoundException) {
            return PARTIDA_NAO_ENCONTRADA;
        }
        if (e instanceof SalaNotFoundException) {
            return PIN_NAO_ENCONTRADO;
        }
        if (e instanceof EmailJaCadastradoException) {
            return EMAIL_JA_CADASTRADO;
        }
        if (e instanceof PartidaInvalidaException) {
            return PARTIDA_INVALIDA;
        }
        if (e instanceof SalaInvalidaException sala) {
            return sala.codigo();
        }
        if (e instanceof JogadaInvalidaException jogada) {
            return valueOf(jogada.getMotivo().name());
        }
        return ERRO_INTERNO;

        
    }
}