# PRD-0033. Quebra de linha no maven-wrapper e .gitattributes

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Corrigir o build do Docker: depois de remover o checksum, o `maven-wrapper.properties` ficou sem quebra de linha no final, e o script `mvnw` (no Linux) não lia a última linha, dando `cannot read distributionUrl property`. Além disso, o `.gitattributes` estava com conteúdo errado (uma cópia do `application.properties`), então a normalização de fim de linha não acontecia.

## Requisitos

- `maven-wrapper.properties` termina com quebra de linha (em LF), para o `mvnw` ler o `distributionUrl`.
- `.gitattributes` com as regras corretas: repositório em LF; `mvnw`, `*.sh` e o `maven-wrapper.properties` sempre em LF; `*.cmd` e `*.bat` em CRLF.

## Critérios de aceite

- O `mvnw` lê o `distributionUrl` e baixa o Maven no build do Docker.
- Arquivos sensíveis ao fim de linha ficam em LF, mesmo editados no Windows.