package br.sousa;

import io.quarkus.oidc.client.Tokens;

public class TokenResponse {

    public String accessToken;
    public Long accessTokenExpiresAt;
    public String refreshToken;

    public TokenResponse(Tokens tokens) {
        this.accessToken = tokens.getAccessToken();
        this.accessTokenExpiresAt = tokens.getAccessTokenExpiresAt();
        this.refreshToken = tokens.getRefreshToken();
    }

}