/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXORequestSecurityLevelAdapter;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class RequestSecurityDeviceAccessLevelResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetRequestSecurityDeviceAccessLevelsPaged() throws Exception {
        EncryptionDeviceAccessLevel accessLevel = mockEncryptionAccessLevel(2);
        SecuritySuite securitySuite = mockSecuritySuite(3);
        RequestSecurityLevel requestSecurityLevel = mockRequestSecurityDeviceAccessLevel(4);
        ResponseSecurityLevel responseSecurityLevel = mockResponseSecurityDeviceAccessLevel(5);
        mockPluggableClass(77, "WebRTU", "1.2.3.4", Collections.emptyList(), Collections.singletonList(accessLevel), Collections.singletonList(securitySuite), Collections.singletonList(requestSecurityLevel), Collections.singletonList(responseSecurityLevel));
        Response response = target("/pluggableclasses/77/requestsecurityaccesslevels").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses/77/requestsecurityaccesslevels?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(4);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Proper name for 4");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/requestsecurityaccesslevels/4");
    }

    @Test
    public void testGetSingleRequestSecurityDeviceAccessLevelWithFields() throws Exception {
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(77, "WebRTU", "1.2.3.4");
        com.energyict.mdc.upl.security.RequestSecurityLevel accessLevel = CXORequestSecurityLevelAdapter.adaptTo(mockRequestSecurityDeviceAccessLevel(3));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getRequestSecurityLevels()).thenReturn(Collections.singletonList(accessLevel));

        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Response response = target("/pluggableclasses/77/requestsecurityaccesslevels/3").queryParam("fields", "id").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(3);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.name")).isNull();
        assertThat(model.<String>get("$.properties")).isNull();
    }


    @Test
    public void testRequestSecurityDeviceAccessLevelFields() throws Exception {
        Response response = target("/pluggableclasses/x/requestsecurityaccesslevels").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "properties", "name");
    }
}