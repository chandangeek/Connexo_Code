package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;


public class TokenResourceTest extends OAuthBaseTest {

    @BeforeClass
    public static void startUp() {
    }

    @Test
    public void shouldReturnToken() {
        final Form tokenRequestForm = new Form();
        tokenRequestForm.param("grant_type", "authorization_code");

        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.form(tokenRequestForm))
                .invoke();

        assertThat(httpResponse.getStatus()).isEqualTo(200);

        final TokenResponse tokenResponse = httpResponse.readEntity(TokenResponse.class);

        assertThat(tokenResponse.getAccessToken()).isNotEmpty();
        assertThat(tokenResponse.getTokenType()).containsSequence("bearer");
        assertThat(tokenResponse.getExpirationDate()).isNotEmpty();
    }
}