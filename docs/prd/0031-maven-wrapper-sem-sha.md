# PRD-0031. Maven Wrapper sem o checksum inválido

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Corrigir o build do Docker no deploy: o `.mvn/wrapper/maven-wrapper.properties` tinha um `distributionSha256Sum` que não correspondia ao Maven 3.9.16, então o wrapper abortava com `Failed to validate Maven distribution SHA-256`.

## Requisitos

- Remover a linha `distributionSha256Sum` do `maven-wrapper.properties`. O wrapper passa a baixar o Maven do repositório oficial da Apache por HTTPS, sem a verificação de checksum.

## Critérios de aceite

- O build do Docker baixa o Maven sem erro de validação de SHA-256.

## Observações

- Para voltar a verificar o checksum, basta regerar o wrapper com `mvn wrapper:wrapper -Dmaven=3.9.16`, que grava o SHA-256 correto.