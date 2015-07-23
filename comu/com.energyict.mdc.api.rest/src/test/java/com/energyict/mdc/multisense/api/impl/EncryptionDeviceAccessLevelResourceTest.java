package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptionDeviceAccessLevelResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetEncryptionDeviceAccessLevelsPaged() throws Exception {
        EncryptionDeviceAccessLevel accessLevel = mockEncryptionAccessLevel(2);
        mockPluggableClass(77, "WebRTU", "1.2.3.4", Collections.emptyList(), Collections.singletonList(accessLevel));
        Response response = target("/pluggableclasses/77/encryptionaccesslevels").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses/77/encryptionaccesslevels?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(2);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Proper name for 2");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/77/encryptionaccesslevels/2");
    }

    @Test
    public void testGetSingleEncryptionDeviceAccessLevelWithFields() throws Exception {
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(77, "WebRTU", "1.2.3.4");
        EncryptionDeviceAccessLevel accessLevel = mockEncryptionAccessLevel(3);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(accessLevel));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        Response response = target("/pluggableclasses/77/encryptionaccesslevels/3").queryParam("fields","id").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(3);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.name")).isNull();
        assertThat(model.<String>get("$.properties")).isNull();
    }



    @Test
    public void testEncryptionDeviceAccessLevelFields() throws Exception {
        Response response = target("/pluggableclasses/x/encryptionaccesslevels").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "properties", "name");
    }


}
