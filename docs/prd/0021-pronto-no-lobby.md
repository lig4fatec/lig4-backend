# PRD-0021. "Pronto" dos dois jogadores para começar a partida

- **Data:** 2026-11-18
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

A partida só começa quando os dois jogadores marcam "pronto" no lobby.

## Requisitos

- `PUT /salas/{pin}/pronto` com `{jogadorId, pronto}` marca ou desmarca o "pronto" de quem está na sala (dono ou convidado).
- A sala devolve `prontoDono` e `prontoConvidado`.
- Quando o convidado está na sala e os dois estão prontos, o back cria a partida sozinho (o dono é o jogador 1 e começa), a sala passa para `JOGANDO`, `partidaId` vem preenchido e as duas marcas voltam a falso.
- O "pronto" só vale com a sala em `AGUARDANDO` (depois, `409 FASE_INVALIDA`). Quem não está na sala recebe `403 SEM_PERMISSAO`. Sem o campo `pronto`, `400 DADOS_INVALIDOS`.
- O dono pode marcar "pronto" antes de o convidado entrar.
- Trocar de cor desmarca o "pronto" de quem trocou. Quando o convidado sai, as duas marcas voltam a falso.
- O endpoint `POST /salas/{pin}/iniciar` deixa de existir: o início passa a ser consequência dos dois "prontos".
- Migration V12: colunas `pronto_dono` e `pronto_convidado` na sala (padrão falso).
- A skin do tabuleiro não passa pelo back: cada jogador usa o próprio `tabuleiro` equipado na loja e o back nunca envia a skin de um jogador ao outro.

## Fora do escopo

- A revanche continua como está: começa quando os dois pedem, sem passar pelo "pronto".
- Aviso em tempo real do "pronto" (o front continua consultando `GET /salas/{pin}` a cada 2 segundos).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Um "pronto" sozinho não inicia a partida, e os dois iniciam.
- Desmarcar, trocar de cor ou o convidado sair impedem o início.