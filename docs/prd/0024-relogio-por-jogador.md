# PRD-0024. Relógio por jogador

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a decisão de 12/11 com o front-end: cada jogador tem um tempo total na partida, como no xadrez, e quem fica sem tempo perde.

## Requisitos

- Cada jogador começa a partida com o mesmo tempo total (padrão: 300 segundos). O tempo não é por jogada: só corre o relógio de quem está na vez, e o que sobra continua para a jogada seguinte.
- O relógio do jogador 1 (quem joga primeiro) começa a correr quando a partida é criada.
- A cada jogada o servidor debita o tempo gasto por quem jogou e passa o relógio ao adversário. Quando a partida termina (vitória, empate, desistência), o relógio para.
- O estado da partida e a resposta de cada jogada trazem `relogioDe` (de quem é o relógio que corre, ou nulo se a partida terminou), `tempoJogadorUmMs` e `tempoJogadorDoisMs`. O front só exibe a contagem.
- Quando o tempo de quem está na vez acaba, o servidor encerra a partida por conta própria, em até 1 segundo: o adversário vence, `motivoFim` é `TEMPO`, as moedas são pagas como numa vitória e na derrota do modo, a sala termina e os dois jogadores recebem o `ESTADO` pelo WebSocket.
- Jogar com o tempo esgotado devolve `409 JOGO_ENCERRADO`.
- O tempo por jogador é configurado no servidor pela propriedade `lig4.relogio.tempo-por-jogador-segundos` (variável de ambiente `LIG4_RELOGIO_TEMPO_POR_JOGADOR_SEGUNDOS`). O host da sala não escolhe o tempo.
- Partidas criadas antes desta mudança não têm relógio e nunca expiram.
- Migration V14: colunas de tempo de cada jogador, de quem é o relógio, quando foi marcado e o prazo na partida.

## Fora do escopo

- Aviso na tela de quem perdeu a vez (cabe ao front, com os tempos do estado).
- Pausa do relógio por desconexão.
- Tempo configurável pelo host na criação da sala.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Quem não joga dentro do tempo perde por `TEMPO` e o adversário recebe a vitória.
- A jogada passa o relógio ao adversário, e a partida terminada para o relógio.