# PRD-0032. Build do Docker simplificado

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Corrigir o build do Docker: o passo `./mvnw -B -q dependency:go-offline` do `Dockerfile` falhava (exit code 1), e o `-q` (quiet) escondia o erro real no log.

## Requisitos

- Remover o passo `dependency:go-offline` do `Dockerfile` (ele tenta resolver dependências que o build normal nem usa e falha à toa).
- Remover o `-q` dos comandos do `mvnw`, para que erros do Maven apareçam no log.
- O `clean package -DskipTests` baixa as dependências por conta própria.

## Critérios de aceite

- A imagem compila e empacota o `.jar` no Docker.
- Se houver erro de compilação, ele fica visível no log do build.