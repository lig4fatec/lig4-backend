# 0003. Adoção de Spring Boot como framework do back-end

- **Data:** 2026-10-21
- **Estado:** Proposta

## Contexto

O [ADR 0001](0001-estrutura-de-documentacao-e-uso-de-ia.md) foi escrito quando a stack ainda não estava definida, e nenhum ADR registrou a escolha posterior. O esqueleto inicial do back-end (commit `PRD-0001`) foi criado com Quarkus 3, Java 25, Maven, PostgreSQL e Flyway.

Durante a implementação, a equipe decidiu migrar para Spring Boot por considerá-lo de utilização mais simples para o time. O prazo de entrega do back-end (31/10/2026) torna o custo da migração relevante, por isso a decisão precisa ser registrada e comunicada ao subgrupo de front-end e à gestão do projeto.

Alternativas consideradas:

- **Manter o Quarkus:** não exige reescrita, mas a equipe o considera menos simples de usar.
- **Migrar para Spring Boot:** exige portar o CRUD e os testes; as migrations, os DTOs e o motor de regras são reaproveitados.

## Decisão

Adotar a seguinte stack para o `lig4-backend`:

- **Linguagem e build:** Java 25 e Maven.
- **Framework:** Spring Boot 4.0.0 (Spring MVC, Spring Data JPA com Hibernate, Bean Validation).
- **Banco de dados:** PostgreSQL, com schema versionado por Flyway.
- **Documentação da API:** springdoc-openapi (Swagger UI em `/swagger-ui`).
- **Comunicação em tempo real com o front-end:** WebSocket (`spring-boot-starter-websocket`), a ser implementado em PRD próprio.
- **Motor de regras:** pacote `br.com.lig4.engine` em Java puro, sem dependência do framework, para ser testável de forma isolada e reaproveitável pela API REST e pelo WebSocket.

## Consequências

- **Positivas:** framework considerado mais simples pela equipe; stack documentada, o que remove a lacuna entre o ADR 0001 e o código; motor de regras independente do framework, o que reduz o custo de uma eventual troca futura.
- **Negativas / trade-offs:** custo da migração dentro de um prazo curto; as entidades usam associações `EAGER` e campos públicos para manter o código simples, o que deve ser revisto se o modelo crescer; Spring Boot 4 e springdoc 3 são versões recentes e podem exigir ajustes de dependência.
- **Neutras:** TAP e WBS que mencionam Quarkus devem ser atualizados pela gestão; o [ARCHITECTURE.md](../../ARCHITECTURE.md) e o [AGENTS.md](../../AGENTS.md) passam a refletir a stack definida.
