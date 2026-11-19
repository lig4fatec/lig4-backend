# PRD-0022. Desistência e proposta de empate

- **Data:** 2026-11-18
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a decisão do grupo de 17/11: o jogador pode desistir da partida e propor empate ao adversário.

## Requisitos

- A partida passa a guardar como terminou (`motivoFim`): `VITORIA`, `TABULEIRO_CHEIO`, `DESISTENCIA`, `EMPATE_ACORDADO` ou `TEMPO` (este último reservado para o relógio). Fica vazio enquanto ela está em andamento.
- **Desistir:** o adversário vence, a partida e a sala terminam, e cada jogador recebe as moedas de vitória e de derrota do modo (quem desiste recebe as moedas de derrota).
- **Propor empate:** cada jogador pode propor uma vez por partida e só uma proposta fica aberta por vez.
  - O adversário aceita (empate, `EMPATE_ACORDADO`, moedas de empate do modo) ou recusa (a partida continua).
  - Quando o adversário joga sem responder, a proposta é recusada. A jogada de quem propôs não cancela a proposta.
- Comandos do WebSocket: `DESISTIR`, `PROPOR_EMPATE`, `ACEITAR_EMPATE` e `RECUSAR_EMPATE`. Depois de cada um, o servidor envia o `ESTADO` aos dois jogadores conectados.
- Endpoints HTTP equivalentes: `POST /partidas/{id}/desistir`, `/empate`, `/empate/aceitar` e `/empate/recusar`, com `{jogadorId}`. Eles devolvem o estado e também avisam quem está conectado.
- O estado da partida passa a trazer `motivoFim`, `empatePropostoPor` e `empatesUsados`. Uma partida terminada por desistência ou empate combinado mostra `statusPartida` `FINALIZADA`, o `resultado` e o `vencedorId` corretos e `proximoJogadorId` nulo.
- Novos códigos de erro (409): `EMPATE_JA_PROPOSTO`, `EMPATE_PENDENTE` e `SEM_PROPOSTA_DE_EMPATE`. Partida já terminada devolve `JOGO_ENCERRADO`, e quem não participa recebe `PARTIDA_INVALIDA`.
- Migration V13: colunas `motivo_fim`, `empate_pendente_de_id`, `empate_usado_um` e `empate_usado_dois` na partida.

## Fora do escopo

- Desistência automática por inatividade ou desconexão (partida abandonada continua sem ser finalizada).
- O motivo `TEMPO` só passa a ser usado no relógio da partida (PRD-0024).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Desistir dá a vitória ao adversário, paga as moedas uma única vez e encerra a sala.
- Empate aceito termina a partida em empate; recusado, ela continua e o proponente não pode propor de novo.