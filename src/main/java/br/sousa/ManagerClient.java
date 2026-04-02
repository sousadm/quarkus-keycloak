package br.sousa;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "apimanager")
public interface ManagerClient {

    @POST
    @Path("/realms/teste/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse getToken(
        @FormParam("grant_type")    String grantType,
        @FormParam("client_id")     String clientId,
        @FormParam("client_secret") String clientSecret,
        @FormParam("scope")         String scope
    );
    
}