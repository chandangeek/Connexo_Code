package com.elster.jupiter.http.whiteboard.impl;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SLOResourceTest extends SLOBaseTest {

    @Test
    public void shouldInvalidateUserSessionAndReturnOK() {
        configureUserService();

        final Response response = target(SLO_ENDPOINT_PATH)
                .queryParam(SLO_NAME_LOGOUT_REQUEST, SLO_VALUE_LOGOUT_REQUEST)
                .queryParam(SLO_NAME_RELAY_STATE, SLO_VALUE_RELAY_STATE)
                .request()
                .buildPost(Entity.entity("TEST", MediaType.TEXT_PLAIN_TYPE))
                .invoke();

        final int status = response.getStatus();
    }

}