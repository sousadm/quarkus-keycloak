Não consigo gerar arquivos diretamente para download, mas posso te ajudar de duas formas:

## Opção 1: Copiar e colar manualmente

Copie o conteúdo do `README.md` que gerei na resposta anterior, cole em um arquivo novo e salve como `README.md` na raiz do seu projeto.

## Opção 2: Criar via terminal

Se estiver no Linux/Mac/Git Bash do Windows, execute:

```bash
cat > README.md << 'EOF'
# Keycloak Local

Guia de Configuração e Teste com Quarkus

*REST Client · OAuth2 · application.properties*

## 📋 Pré-requisitos

Antes de iniciar, certifique-se de que os seguintes itens estão instalados e disponíveis:

| Ferramenta | Versão mínima / Observação |
|------------|----------------------------|
| Docker | 20.x ou superior |
| Java (JDK) | 17 ou superior |
| Quarkus CLI / Maven | Quarkus 3.x |
| Insomnia / curl | Qualquer versão recente |

## 🐳 Subindo o Keycloak com Docker

### Execução simples (sem persistência)

Use apenas para testes rápidos. Os dados são perdidos ao parar o container:

```bash
docker run -p 8180:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

> **⚠️ Atenção:** Sem volume, toda configuração (realm, clients, usuários) é perdida ao parar o container.

### Execução com volume persistente (recomendado)

O flag `-v` garante que os dados sobrevivam a reinicializações:

```bash
docker run -p 8180:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -v keycloak_data:/opt/keycloak/data \
  quay.io/keycloak/keycloak:latest start-dev
```

### Docker Compose (melhor para projetos)

Crie um arquivo `docker-compose.yml` na raiz do projeto:

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    command: start-dev
    ports:
      - "8180:8080"
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    volumes:
      - keycloak_data:/opt/keycloak/data

volumes:
  keycloak_data:
```

**Comandos do Docker Compose:**

| Comando | Efeito |
|---------|--------|
| `docker-compose up -d` | Sobe em background |
| `docker-compose stop` | Para — dados preservados |
| `docker-compose down` | Remove container — dados preservados |
| `docker-compose down -v` | ⚠️ Remove container E volume (apaga tudo) |

## ⚙️ Configurando o Keycloak

Acesse `http://localhost:8180` com usuário `admin` e senha `admin`.

### Criar Realm

- Clique em **master** (canto superior esquerdo)
- Selecione **Create Realm**
- Name: `teste`
- Clique em **Create**

### Criar Client

- No menu lateral, acesse **Clients** → **Create client**
- Client ID: `meu-client`
- Client authentication: **ON**
- Authentication flow: marque apenas **Service accounts roles**
- Clique em **Save**

### Obter o Client Secret

- Acesse a aba **Credentials** do client criado
- Copie o valor de **Client secret** — será usado no `application.properties`

> **✅ Dica:** Com o volume configurado, este setup só precisa ser feito uma vez. O realm e o client persistem entre reinicializações.

## 📝 Configuração — application.properties

Adicione as seguintes propriedades ao arquivo `src/main/resources/application.properties`:

```properties
# ── REST Client — Keycloak ──────────────────────────────
quarkus.rest-client.keycloak.url=http://localhost:8180
quarkus.rest-client.keycloak.scope=jakarta.inject.Singleton

# ── Logging ─────────────────────────────────────────────
quarkus.rest-client.logging.scope=request-response
quarkus.rest-client.logging.body-limit=5120

# ── Credenciais OAuth2 ──────────────────────────────────
app.auth.grant-type=client_credentials
app.auth.client-id=meu-client
app.auth.client-secret=${AUTH_CLIENT_SECRET:COLE_SEU_SECRET_AQUI}
app.auth.scope=openid
```

> **🔒 Segurança:** Em produção, nunca coloque o client-secret diretamente no arquivo. Use variáveis de ambiente:
> ```bash
> export AUTH_CLIENT_SECRET=seu-secret
> ```

### Múltiplas APIs — namespace por cliente

Para cada API externa, use um namespace diferente no configKey:

```properties
# ── Keycloak ────────────────────────────────────────────
quarkus.rest-client.keycloak.url=http://localhost:8180
app.auth.keycloak.client-id=meu-client
app.auth.keycloak.client-secret=secret-keycloak

# ── API de Pagamentos ───────────────────────────────────
quarkus.rest-client.pagamentos.url=https://api.pagamentos.com
app.auth.pagamentos.client-id=pag-client
app.auth.pagamentos.client-secret=secret-pagamentos
```

## 💻 Código Java

### Interface — KeycloakClient

```java
package org.example.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.example.model.TokenResponse;

@RegisterRestClient(configKey = "keycloak")
@Path("/realms/teste/protocol/openid-connect/token")
public interface KeycloakClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse getToken(
        @FormParam("grant_type") String grantType,
        @FormParam("client_id") String clientId,
        @FormParam("client_secret") String clientSecret,
        @FormParam("scope") String scope
    );
}
```

### Model — TokenResponse

```java
package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("refresh_token")
    public String refreshToken; // null em client_credentials

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public Long expiresIn;

    @JsonProperty("scope")
    public String scope;
}
```

### Service — AuthService

```java
package org.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.client.KeycloakClient;
import org.example.model.TokenResponse;

@ApplicationScoped
public class AuthService {

    @RestClient
    KeycloakClient keycloakClient;

    @ConfigProperty(name = "app.auth.grant-type")
    String grantType;

    @ConfigProperty(name = "app.auth.client-id")
    String clientId;

    @ConfigProperty(name = "app.auth.client-secret")
    String clientSecret;

    @ConfigProperty(name = "app.auth.scope")
    String scope;

    public TokenResponse obterToken() {
        return keycloakClient.getToken(grantType, clientId, clientSecret, scope);
    }
}
```

### Controller — AuthController

```java
package org.example.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.model.TokenResponse;
import org.example.service.AuthService;

@Path("/auth")
public class AuthController {

    @Inject
    AuthService authService;

    @GET
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse getToken() {
        return authService.obterToken();
    }
}
```

## 📦 Dependências — pom.xml

Adicione as dependências necessárias:

```xml
<!-- Servidor REST (já existente) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
</dependency>

<!-- Cliente REST para APIs externas (ADICIONAR) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-reactive-jackson</artifactId>
</dependency>
```

| Dependência | Função |
|-------------|--------|
| `quarkus-rest-jackson` | Expõe seus endpoints REST (servidor) |
| `quarkus-rest-client-reactive-jackson` | Chama APIs externas (cliente) |

## 🧪 Testando com Insomnia

### Testar diretamente o Keycloak

- **Método:** `POST`
- **URL:** `http://localhost:8180/realms/teste/protocol/openid-connect/token`
- **Body → Form URL Encoded:**

| Key | Value |
|-----|-------|
| grant_type | client_credentials |
| client_id | meu-client |
| client_secret | {valor copiado do Keycloak} |
| scope | openid |

### Testar via Quarkus

- **Método:** `GET`
- **URL:** `http://localhost:8080/auth/token`

### Resposta esperada

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5...",
  "refreshToken": null,
  "tokenType": "Bearer",
  "expiresIn": 300,
  "scope": "openid profile email"
}
```

> **ℹ️ Nota:** No fluxo `client_credentials` o `refresh_token` não é retornado. Isso é comportamento padrão do OAuth2 para comunicação máquina a máquina.

## 🐛 Erros Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| 401 Unauthorized | client_secret incorreto | Recopiar da aba Credentials |
| 400 Bad Request | grant_type ausente ou Service accounts desabilitado | Verificar configuração do client |
| Connection refused | Docker não está rodando | Verificar `docker ps` |
| Realm not found | Nome do realm diferente na URL | Confirmar nome: `/realms/teste` |
| ClassNotFoundException | Falta `quarkus-rest-client-reactive-jackson` | Adicionar dependência no pom.xml |
| 500 Internal Server Error | configKey não bate com o properties | Verificar configKey = "keycloak" |

## ✅ Checklist de Verificação

- [ ] Docker rodando com o container do Keycloak
- [ ] Realm `teste` criado no Keycloak
- [ ] Client `meu-client` criado com **Service accounts roles** ativado
- [ ] Client secret copiado para o `application.properties`
- [ ] Dependência `quarkus-rest-client-reactive-jackson` no `pom.xml`
- [ ] `configKey = "keycloak"` na interface bate com o properties
- [ ] Quarkus rodando na porta `8080`
- [ ] Teste via Insomnia retornando `access_token`
EOF
```

Isso criará o arquivo `README.md` no diretório atual.

## Opção 3: Criar via PowerShell (Windows)

```powershell
@"
# Keycloak Local

Guia de Configuração e Teste com Quarkus

[Conteúdo completo do README.md]
"@ | Out-File -FilePath README.md -Encoding utf8
```

**Recomendo a Opção 1** (copiar e colar) - é a mais simples e direta. Basta criar um novo arquivo no seu editor de código (VS Code, IntelliJ, etc.), colar o conteúdo e salvar como `README.md` na raiz do projeto.
