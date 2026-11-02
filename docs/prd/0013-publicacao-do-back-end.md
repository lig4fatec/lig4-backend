# PRD-0013. Publicação do back-end

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Permitir que o back-end rode em um servidor de hospedagem, e não só no computador de quem desenvolve, para o front-end e a apresentação usarem um endereço público.

## Requisitos

- `Dockerfile` em dois estágios (compilação com JDK 25 e execução com JRE 25) e `.dockerignore` próprio do Spring Boot.
- Porta (`PORT`) e banco (`DB_URL`, `DB_USER`, `DB_PASSWORD`) por variável de ambiente, com os valores locais como padrão.
- Pool de conexões pequeno, que devolve conexões ociosas, para o banco gratuito poder dormir.
- `GET /saude` responde `{"status":"ok"}` para a verificação de saúde da hospedagem.
- Uso correto de `https` e `wss` atrás do proxy (`server.forward-headers-strategy=framework`).
- `.gitattributes` mantém o `mvnw` com quebra de linha do Linux.
- `docs/DEPLOY.md` descreve o passo a passo com Neon, Render e o front.

## Fora do escopo

- A criação das contas e a publicação em si, feitas por quem tem acesso à organização no GitHub.
- Domínio próprio, monitoramento e backup do banco.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (111 testes).
- `GET /saude` devolve 200 com `{"status":"ok"}`.
- Sem nenhuma variável de ambiente definida, o servidor continua subindo localmente como antes.