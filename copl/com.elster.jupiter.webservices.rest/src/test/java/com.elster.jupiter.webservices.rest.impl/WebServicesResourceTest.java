/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/10/16.
 */
public class WebServicesResourceTest extends WebServicesApplicationTest {

    @Test
    public void testGetWebServices() throws Exception {
        WebService mock = mockWebService("ws1", true);
        WebService mock2 = mockWebService("ws2", false);
        when(webServicesService.getWebServices()).thenReturn(Arrays.asList(mock, mock2));
        Response response = target("/webservices").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("webServices[0].name")).isEqualTo("ws1");
        assertThat(jsonModel.<String>get("webServices[0].direction.id")).isEqualTo("INBOUND");
        assertThat(jsonModel.<String>get("webServices[0].direction.localizedValue")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("webServices[0].type")).isEqualTo("SOAP");
        assertThat(jsonModel.<String>get("webServices[1].name")).isEqualTo("ws2");
        assertThat(jsonModel.<String>get("webServices[1].direction.id")).isEqualTo("OUTBOUND");
        assertThat(jsonModel.<String>get("webServices[1].direction.localizedValue")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("webServices[1].type")).isEqualTo("SOAP");
    }

    private WebService mockWebService(String name, boolean inbound) {
        WebService mock = mock(WebService.class);
        when(mock.getName()).thenReturn(name);
        when(mock.isInbound()).thenReturn(inbound);
        when(mock.getProtocol()).thenReturn(WebServiceProtocol.SOAP);
        return mock;
    }
}
