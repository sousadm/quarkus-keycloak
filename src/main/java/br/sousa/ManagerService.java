package br.sousa;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ManagerService {

	@RestClient
	ManagerClient apiManagerClient;

	@ConfigProperty(name = "app.auth.keycloak.grant-type")
	String grantType;

	@ConfigProperty(name = "app.auth.keycloak.client-id")
	String clientId;

	@ConfigProperty(name = "app.auth.keycloak.client-secret")
	String clientSecret;

	@ConfigProperty(name = "app.auth.keycloak.scope")
	String scope;

	public TokenResponse obterToken() {
		return apiManagerClient.getToken(grantType, clientId, clientSecret, scope);
	}

}
