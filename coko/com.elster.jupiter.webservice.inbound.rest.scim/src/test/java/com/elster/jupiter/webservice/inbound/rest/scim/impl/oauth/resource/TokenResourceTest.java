package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;


public class TokenResourceTest extends OAuthBaseTest {

    @BeforeClass
    public static void startUp() {
    }

    @Test
    public void shouldReturnToken() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(200);

        final TokenResponse tokenResponse = httpResponse.readEntity(TokenResponse.class);

        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getTokenType()).containsSequence("bearer");
        assertThat(tokenResponse.getExpirationDate()).isNotEmpty();

        final Jwt<?, ?> jwt = parseJws(tokenResponse.getAccessToken());

        assertThat(jwt).isNotNull();

        final Header<?> header = jwt.getHeader();
        assertThat(header.get("alg").toString()).contains("HS512");

        final Claims body = (Claims) jwt.getBody();
        assertThat(body.get("sub").toString()).contains("enexis");
        assertThat(body.get("exp").toString()).isNotEmpty();
        assertThat(body.get("iat").toString()).isNotEmpty();
    }

    @Test
    public void shouldReturnInvalidRequestErrorWhenGrantTypeIsNotSpecified() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITHOUT_GRANT_TYPE))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturnInvalidRequestErrorWhenGrantTypeIsNotSupported() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(400);
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsMalformed() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "somerandomstringhere")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(401);
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsNotSpecified() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(401);
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsNotSetToBasic() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + CLIENT_CREDENTIALS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(401);
    }
}