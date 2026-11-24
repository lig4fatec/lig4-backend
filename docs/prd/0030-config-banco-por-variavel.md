# PRD-0030. Configuração do banco por variável de ambiente

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Corrigir o deploy: o `application.properties` tinha a URL e o usuário do banco fixos em `localhost`/`postgres`, e só a senha vinha de variável. No Render, as variáveis `DB_URL` e `DB_USER` eram ignoradas e a aplicação tentava conectar no `localhost`, dando `Connection refused`.

## Requisitos

- `spring.datasource.url`, `spring.datasource.username` e `spring.datasource.password` passam a ler as variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`, com os valores locais como padrão (`${DB_URL:jdbc:postgresql://localhost:5432/lig4}` etc.).
- Local continua funcionando sem variável; no deploy, os três valores vêm do Neon.

## Critérios de aceite

- Sem variáveis, sobe no Postgres local.
- No Render, conecta no Neon com `DB_URL`/`DB_USER`/`DB_PASSWORD`.