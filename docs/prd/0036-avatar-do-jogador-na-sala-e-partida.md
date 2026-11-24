# PRD-0036. Avatar do jogador na sala e na partida

- **Data:** 2026-11-23
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

No lobby e na partida, o front não carregava o avatar do adversário (só o avatar padrão), porque o back não mandava o avatar de ninguém: os objetos de jogador da sala (`SalaJogadorDTO`) e da partida (`JogadorResumoDTO`) tinham só `{id, nome, cor}`. Este PRD inclui o avatar equipado nesses objetos.

## Requisitos

- `SalaJogadorDTO` e `JogadorResumoDTO` ganham o campo `avatar`, preenchido com o `avatarEquipado` do jogador.
- A sala (`dono`/`convidado`) e o estado da partida (`jogadorUm`/`jogadorDois`) passam a trazer `{id/jogadorId, nome, cor, avatar}`.
- O front monta a imagem a partir do id do avatar, como já faz na loja.

## Decisões

- O `nome` (apelido) já era enviado; só faltava o avatar. O nick aparecer como "jogador 2" no front é mapeamento do adaptador (ler `nome`), não é mudança de back.

## Fora do escopo

- Board/emote equipado do adversário (se precisarem, é o mesmo padrão).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- `GET /salas/{pin}` e `GET /partidas/{id}/estado` trazem o `avatar` de cada jogador.
