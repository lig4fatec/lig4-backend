package br.com.lig4.domain;

/** O que uma linha da tabela de jogadas registra: uma peça solta ou o uso de um poder. */
public enum TipoJogada {
    PECA,
    QUEIMAR,
    CONGELAR,
    EXTRA
}