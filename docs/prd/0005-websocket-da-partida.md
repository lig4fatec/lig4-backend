# PRD-0005. Comunicação em tempo real da partida via WebSocket

- **Data:** 2026-10-21
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Permitir que os dois jogadores de uma partida joguem em tempo real, recebendo cada jogada assim que ela é aceita, sem precisar consultar a API repetidamente (WBS 1.5.2.1, 1.5.2.2 e base da 1.5.3).

## Requisitos

- Endpoint WebSocket `/ws/partidas/{partidaId}?jogadorId={id}`. Jogador que não participa da partida, ou partida inexistente, é recusado com uma mensagem de erro.
- Ao conectar, o servidor envia o estado atual da partida, o que também permite reconectar e ressincronizar.
- O cliente envia `{"tipo":"JOGAR","coluna":N}` para jogar e `{"tipo":"ESTADO"}` para pedir o estado.
- A cada jogada aceita, o servidor envia `{"tipo":"JOGADA","dados":{...}}` aos dois jogadores conectados, com a posição, o tabuleiro, o resultado e o próximo jogador.
- Erros (fora do turno, coluna inválida ou cheia, partida encerrada, mensagem inválida) são enviados só a quem os causou, como `{"tipo":"ERRO","mensagem":"..."}`.
- As regras e a persistência são as mesmas da API REST (`JogadaService`).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas, incluindo os testes do WebSocket.
- Ao conectar, o jogador recebe o estado atual.
- Uma jogada feita por um jogador chega aos dois.
- Uma jogada fora do turno devolve erro apenas ao jogador que a fez.
- Um jogador que não participa da partida é recusado.

## Pontos em aberto

- Autenticação: a identificação do jogador pelo parâmetro `jogadorId` não é segura e deve ser substituída quando houver login.
- Origens permitidas: hoje liberadas para qualquer origem, para desenvolvimento; restringir antes de publicar.