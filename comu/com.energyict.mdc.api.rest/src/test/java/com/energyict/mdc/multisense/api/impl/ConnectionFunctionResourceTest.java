/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.UPLConnectionFunction;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Stijn Vanhoorelbeke
 * @since 24.08.17 - 14:41
 */
public class ConnectionFunctionResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetConnectionFunctionsPaged() throws Exception {
        UPLConnectionFunction connectionFunction_1 = mockUPLConnectionFunction(1, "CF_1");
        UPLConnectionFunction connectionFunction_2 = mockUPLConnectionFunction(2, "CF_2");
        UPLConnectionFunction connectionFunction_3 = mockUPLConnectionFunction(3, "CF_3");
        mockPluggableClass(77, "WebRTU", "1.2.3.4", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Arrays.asList(connectionFunction_1, connectionFunction_2),
                Arrays.asList(connectionFunction_2, connectionFunction_3));
        Response response = target("/pluggableclasses/77/connectionfunctions").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses/77/connectionfunctions?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(3);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(1);
        assertThat(model.<String>get("data[0].name")).isEqualTo("CF_1");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/connectionfunctions/1");
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(2);
        assertThat(model.<String>get("data[1].name")).isEqualTo("CF_2");
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/connectionfunctions/2");
        assertThat(model.<Integer>get("data[2].id")).isEqualTo(3);
        assertThat(model.<String>get("data[2].name")).isEqualTo("CF_3");
        assertThat(model.<String>get("data[2].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[2].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/connectionfunctions/3");
    }

    @Test
    public void testGetSingleConnectionFunctionWithFields() throws Exception {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        UPLConnectionFunction connectionFunction_1 = mockUPLConnectionFunction(1, "CF_1");
        UPLConnectionFunction connectionFunction_2 = mockUPLConnectionFunction(2, "CF_2");
        UPLConnectionFunction connectionFunction_3 = mockUPLConnectionFunction(3, "CF_3");
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(77, "WebRTU", "1.2.3.4", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections
                        .emptyList(),
                Arrays.asList(connectionFunction_1, connectionFunction_2),
                Arrays.asList(connectionFunction_2, connectionFunction_3));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Response response = target("/pluggableclasses/77/connectionfunctions/2").queryParam("fields", "id").queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(2);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.name")).isNull();
    }

    @Test
    public void testConnectionFunctionsFields() throws Exception {
        Response response = target("/pluggableclasses/x/connectionfunctions").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(3);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "name");
    }
}