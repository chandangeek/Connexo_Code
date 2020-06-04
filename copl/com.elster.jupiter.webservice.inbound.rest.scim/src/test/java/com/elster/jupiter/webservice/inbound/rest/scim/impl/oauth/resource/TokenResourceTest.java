package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@Ignore
public class TokenResourceTest extends OAuthBaseTest {

    @Test
    public void shouldReturnToken() throws ParseException, JOSEException {
        when(tokenService.createServiceSignedJWT(any(), any(), any(), any(), any()))
                .thenReturn(createServiceSignedJWT(30 * 60 * 1000, "enexis", "connexo", new HashMap<>()));

        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(httpResponse.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(httpResponse.getHeaders().getFirst("Pragma")).isEqualTo("no-cache");

        final TokenResponse tokenResponse = httpResponse.readEntity(TokenResponse.class);

        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getTokenType()).containsSequence("bearer");
        assertThat(tokenResponse.getExpiresIn()).isNotZero().isNotNegative();

        final SignedJWT jwt = parseJws(tokenResponse.getAccessToken());

        assertThat(jwt).isNotNull();

//        final Header<?> header = jwt.getHeader();
//        assertThat(header.get("alg").toString()).contains("HS512");
//
//        final Claims body = (Claims) jwt.getBody();
//        assertThat(body.get("iss").toString()).contains("connexo");
//        assertThat(body.get("sub").toString()).contains("enexis");
//        assertThat(body.get("exp").toString()).isNotEmpty();
//        assertThat(body.get("iat").toString()).isNotEmpty();
//        assertThat(body.get("nbf").toString()).isNotEmpty();
//        assertThat(Long.parseLong(body.get("exp").toString())).isEqualTo(tokenResponse.getExpiresIn());
    }

    @Test
    public void shouldReturnInvalidRequestErrorWhenGrantTypeIsNotSpecified() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITHOUT_GRANT_TYPE))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldReturnInvalidRequestErrorWhenGrantTypeIsNotSupported() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsMalformed() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "somerandomstringhere")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsNotSpecified() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsNotSetToBasic() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + CLIENT_CREDENTIALS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}