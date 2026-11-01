# PRD-0014. Login essencial: cadastro e entrada com senha

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Permitir que o jogador crie uma conta e entre com e-mail e senha, como as telas de cadastro e login do front-end, guardando a senha de forma segura.

## Requisitos

- `POST /auth/cadastro` com `email` e `senha` cria o jogador (201). O `nome` começa como a parte do e-mail antes do `@`.
- `POST /auth/login` com `email` e `senha` devolve o jogador (200) ou `CREDENCIAIS_INVALIDAS` (401), com a mesma resposta para e-mail inexistente e senha errada.
- Erros com código: `EMAIL_INVALIDO` (400), `SENHA_INVALIDA` (400, de 6 a 72 caracteres), `EMAIL_JA_CADASTRADO` (409).
- O e-mail é comparado sem diferenciar maiúsculas e minúsculas.
- A senha é guardada como hash BCrypt na coluna `senha_hash` (migration V7) e nunca aparece nas respostas.
- Jogadores criados por `POST /jogadores` continuam existindo, sem senha, e não conseguem entrar.

## Fora do escopo

- Token, sessão e proteção das rotas. O front guarda o `id` devolvido e o envia como `jogadorId`. A autenticação por token fica para uma próxima etapa.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- A resposta de cadastro e de login não contém a senha nem o hash.