/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOAuthenticationLevelAdapter;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationDeviceAccessLevelResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetAuthenticationDeviceAccessLevelsPaged() throws Exception {
        AuthenticationDeviceAccessLevel accessLevel = mockAuthenticationAccessLevel(2);
        mockPluggableClass(77, "WebRTU", "1.2.3.4", Collections.singletonList(accessLevel), Collections.emptyList());
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/pluggableclasses/77/authenticationaccesslevels").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses/77/authenticationaccesslevels?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(2);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Proper name for 2");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/authenticationaccesslevels/2");
    }

    @Test
    public void testGetSingleAuthenticationDeviceAccessLevelWithFields() throws Exception {
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(77, "WebRTU", "1.2.3.4");
        AuthenticationDeviceAccessLevel accessLevel = mockAuthenticationAccessLevel(3);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels = Collections.singletonList(accessLevel);
        List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> adaptedLevels = authenticationDeviceAccessLevels.stream().map(CXOAuthenticationLevelAdapter::new).collect(Collectors.toList());
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(adaptedLevels);
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Response response = target("/pluggableclasses/77/authenticationaccesslevels/3").queryParam("fields", "id").queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(3);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.name")).isNull();
        assertThat(model.<String>get("$.properties")).isNull();
    }


    @Test
    public void testAuthenticationDeviceAccessLevelFields() throws Exception {
        Response response = target("/pluggableclasses/x/authenticationaccesslevels").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "properties", "name");
    }


}
