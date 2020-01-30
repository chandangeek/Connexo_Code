package com.elster.jupiter.http.whiteboard.impl;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SLOResourceTest extends SLOBaseTest {

    @Test
    public void shouldInvalidateUserSessionAndReturnOK() {
        final Response response = target(SLO_ENDPOINT_PATH)
                .queryParam(SLO_REQUEST_QUERY_PARAM_NAME, SLO_REQUEST_QUERY_PARAM_VALUE)
                .request()
                .buildPost(Entity.entity("TEST", MediaType.TEXT_PLAIN_TYPE))
                .invoke();

        final int status = response.getStatus();
    }

}