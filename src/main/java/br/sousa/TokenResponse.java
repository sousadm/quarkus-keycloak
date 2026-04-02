package br.sousa;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public Long expiresIn;
}