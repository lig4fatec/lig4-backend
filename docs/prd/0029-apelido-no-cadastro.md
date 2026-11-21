# PRD-0029. Apelido no cadastro

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

No front, o cadastro pede um apelido (nickname), mas o back só aceitava e-mail e senha e derivava o nick da parte antes do @. Este PRD faz o back aceitar e guardar o apelido escolhido no cadastro.

## Requisitos

- `POST /auth/cadastro` passa a aceitar o campo `nick` (opcional), de 3 a 20 caracteres.
- Com `nick`, o jogador é criado com esse apelido. Sem `nick` (ou vazio), o nick continua sendo a parte antes do @ do e-mail, como antes.
- `nick` fora de 3 a 20 caracteres devolve `400 NICK_INVALIDO`.
- O apelido pode ser trocado depois no perfil (`PUT /jogadores/{id}/perfil`), como já era.

## Decisões

- O campo é `nick` (o mesmo nome do perfil no back). O front hoje chama de `nickname`; o adaptador do front manda como `nick`.
- O apelido é opcional no back, para não quebrar quem chamar o cadastro só com e-mail e senha. No front ele é obrigatório, e o front valida isso.
- A resposta do cadastro continua trazendo o apelido no campo `nome` (convenção do back); o adaptador do front mapeia para o campo dele.

## Fora do escopo

- Unicidade de apelido (dois jogadores podem ter o mesmo nick, como já acontecia).
- Alinhar o nome do campo na resposta (`nome` vs `nickname`): fica para o adaptador do front.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Cadastro com `nick` cria o jogador com o apelido; sem `nick`, usa a parte do e-mail; `nick` curto é recusado.