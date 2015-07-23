package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceProtocolPluggableClassResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetDeviceProtocolPluggableClasssPaged() throws Exception {
        AuthenticationDeviceAccessLevel accessLevel = mockAuthenticationAccessLevel(2);
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(31L, "WebRTU", "1.9.2.3546", Collections.singletonList(accessLevel));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = mockFinder(Collections.singletonList(pluggableClass));
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        Response response = target("/pluggableclasses").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("WebRTU");
        assertThat(model.<String>get("data[0].version")).isEqualTo("1.9.2.3546");
        assertThat(model.<String>get("$.data[0].javaClassName")).isEqualTo("com.energyict.prot.WebRTU.class");
        assertThat(model.<List>get("$.data[0].authenticationAccessLevels")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].authenticationAccessLevels[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.data[0].authenticationAccessLevels[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].authenticationAccessLevels[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/authenticationaccesslevels/2");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31");
    }

    @Test
    public void testGetSingleDeviceProtocolPluggableClassWithFields() throws Exception {
        mockPluggableClass(31L, "WebRTU", "1.9.2.3546");
        Response response = target("/pluggableclasses/31").queryParam("fields","id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("WebRTU");
        assertThat(model.<String>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testDeviceProtocolPluggableClassFields() throws Exception {
        Response response = target("/pluggableclasses").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(6);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "name", "version", "javaClassName", "authenticationAccessLevels");
    }


}
