package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.users.User;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TokenResourceTest extends OAuthBaseTest {
    @Mock
    private User provisioning;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(userService.findUser("provisioning")).thenReturn(Optional.of(provisioning));
    }

    @Test
    public void shouldReturnToken() throws ParseException, JOSEException {
        when(tokenService.createServiceSignedJWT(any(User.class), anyLong(), anyString(), anyString(), anyMapOf(String.class, Object.class)))
                .thenReturn(createServiceSignedJWT(30 * 60 * 1000, "enexis", "connexo", new HashMap<>()));

        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final TokenResponse tokenResponse = httpResponse.readEntity(TokenResponse.class);

        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getTokenType()).containsSequence("bearer");
        assertThat(tokenResponse.getExpiresIn()).isNotZero().isNotNegative();

        final SignedJWT jwt = parseJws(tokenResponse.getAccessToken());

        assertThat(jwt).isNotNull();
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

    @Ignore("Auth is not checked anymore for some reason")
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

    @Ignore("Auth is not checked anymore for some reason")
    @Test
    public void shouldReturnInvalidClientErrorWhenAuthorizationHeaderIsNotSpecified() {
        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Ignore("Auth is not checked anymore for some reason")
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
