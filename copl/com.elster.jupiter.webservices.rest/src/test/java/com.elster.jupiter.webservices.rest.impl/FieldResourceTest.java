/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 6/15/16.
 */
public class FieldResourceTest extends WebServicesApplicationTest {

    @Test
    public void testLogLevels() throws Exception {
        Response response = target("/fields/logLevel").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.logLevels[0].id")).isEqualTo("SEVERE");
        assertThat(jsonModel.<String>get("$.logLevels[0].localizedValue")).isEqualTo("Severe");

    }

    @Test
    public void testDirections() throws Exception {
        Response response = target("/fields/direction").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.directions[0].id")).isEqualTo("INBOUND");
        assertThat(jsonModel.<String>get("$.directions[0].localizedValue")).isEqualTo("Inbound");

    }

    @Test
    public void testAuthenticationMethods() throws Exception {
        Response response = target("/fields/authenticationMethod").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.authenticationMethods[0].id")).isEqualTo("NONE");
        assertThat(jsonModel.<String>get("$.authenticationMethods[0].localizedValue")).isEqualTo("No authentication");

    }
}
