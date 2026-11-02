# Publicação do back-end

Caminho gratuito: banco no **Neon**, servidor no **Render** (via Docker) e o front como site estático (Netlify ou o próprio Render). Planos gratuitos mudam; confira as telas atuais de cada serviço.

## Variáveis de ambiente do servidor

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST/BANCO?sslmode=require` (do Neon, veja abaixo) |
| `DB_USER` | usuário do Neon |
| `DB_PASSWORD` | senha do Neon |
| `LIG4_ORIGENS_PERMITIDAS` | endereço do front, por exemplo `https://lig4.netlify.app`. Substitui a lista inteira; aceita curinga como `https://*.netlify.app` e várias origens separadas por vírgula |
| `JAVA_OPTS` | opcional; já vem `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC` no Dockerfile |
| `PORT` | o Render define sozinho; não precisa criar |

## 1. Banco no Neon
1. Crie uma conta em neon.tech e um projeto (qualquer região próxima).
2. Em **Connection details**, desligue a opção **Pooled connection** e copie o host **sem** `-pooler`. O Flyway e as travas de linha do jogo funcionam melhor na conexão direta.
3. O Neon mostra uma URL no formato `postgresql://usuario:senha@host/banco?...`. Para o Spring, monte separado:
   - `DB_URL` = `jdbc:postgresql://host/banco?sslmode=require` (sem usuário, senha nem `channel_binding`)
   - `DB_USER` = o usuário
   - `DB_PASSWORD` = a senha
4. O banco gratuito dorme quando fica sem uso; a primeira consulta depois disso demora alguns segundos.

## 2. Servidor no Render
1. Em render.com, crie um **Web Service** a partir do repositório `lig4fatec/lig4-backend`. Como é um repositório de organização, o Render precisa de acesso a ele (instalar o app do Render no GitHub da organização) ou o repositório precisa ser público ("Public Git Repository").
2. Em **Language/Runtime**, escolha **Docker**. O Render usa o `Dockerfile` da raiz.
3. Escolha o plano **Free**. Atenção: com 512 MB de memória e o servidor ocioso por 15 minutos, ele dorme e a primeira chamada seguinte leva cerca de 1 minuto para acordar.
4. Em **Environment**, crie as variáveis da tabela acima.
5. Em **Health Check Path**, informe `/saude`.
6. Publique. O primeiro build demora alguns minutos. Quando terminar, abra `https://SEU-SERVICO.onrender.com/saude`: deve responder `{"status":"ok"}`. Em `/swagger-ui` fica a documentação.

## 3. Front-end
- O front deve usar `https://SEU-SERVICO.onrender.com` para as chamadas REST e **`wss://`** (e não `ws://`) para o WebSocket: `wss://SEU-SERVICO.onrender.com/ws/partidas/{id}?jogadorId={id}`.
- Depois de publicar o front, coloque o endereço dele em `LIG4_ORIGENS_PERMITIDAS` no Render (o serviço reinicia sozinho).

## Dicas para a apresentação
- Acorde o servidor 5 minutos antes (abra `/saude`) e confira que a primeira consulta ao banco também respondeu.
- Para ele não dormir durante o evento, um serviço gratuito de monitoramento (por exemplo, UptimeRobot) pode chamar `/saude` a cada 10 minutos.

## Problemas comuns
| Sintoma | Causa provável |
|---|---|
| Build falha em `./mvnw: not found` ou `^M` | Quebra de linha do Windows no `mvnw`; o `.gitattributes` do projeto resolve |
| `Connection refused` ou erro de SSL ao subir | `DB_URL` sem `?sslmode=require` ou com o host `-pooler` |
| O front recebe erro de CORS | O endereço do front não está em `LIG4_ORIGENS_PERMITIDAS` (confira `https` e o domínio exato) |
| WebSocket não conecta | O front usa `ws://` em vez de `wss://`, ou a origem não está liberada |