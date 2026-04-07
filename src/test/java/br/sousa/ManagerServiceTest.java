package br.sousa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagerServiceTest {

    @Test
    void obterTokenShouldReturnResponseFromClient() {
        ManagerService service = new ManagerService();
        service.apiManagerClient = new ManagerClient() {
            @Override
            public TokenResponse getToken(String grantType, String clientId, String clientSecret, String scope) {
                
                assertEquals("client_credentials", grantType);
                assertEquals("my-client", clientId);
                assertEquals("secret", clientSecret);
                assertEquals("openid", scope);

                TokenResponse response = new TokenResponse();
                response.accessToken = "fake-access-token";
                response.refreshToken = "fake-refresh-token";
                response.scope = scope;
                response.tokenType = "Bearer";
                response.expiresIn = 3600L;
                return response;
            }
        };

        service.grantType = "client_credentials";
        service.clientId = "my-client";
        service.clientSecret = "secret";
        service.scope = "openid";

        TokenResponse token = service.obterToken();

        assertNotNull(token);
        assertEquals("fake-access-token", token.accessToken);
        assertEquals("fake-refresh-token", token.refreshToken);
        assertEquals("openid", token.scope);
        assertEquals("Bearer", token.tokenType);
        assertEquals(3600L, token.expiresIn);
    }
}

