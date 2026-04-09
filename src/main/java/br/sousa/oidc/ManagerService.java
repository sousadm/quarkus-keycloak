package br.sousa.oidc;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ManagerService {

    @Inject
    OidcClient oidcClient;

    public TokenResponse obterToken() {
        Tokens tokens = oidcClient.getTokens().await().indefinitely();
        return new TokenResponse(tokens);
    }
}