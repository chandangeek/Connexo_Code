package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtocolDialectConfigurationPropertiesResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetProtocolDialectConfigurationPropertiesPaged() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(31L);
        when(properties.getName()).thenReturn("west vloms");
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Arrays.asList(properties));

        Response response = target("/devicetypes/21/deviceconfigurations/22/protocoldialectconfigurationproperties").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/protocoldialectconfigurationproperties?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("west vloms");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/protocoldialectconfigurationproperties/31");
    }

    @Test
    public void testGetSingleProtocolDialectConfigurationPropertiesWithFields() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(31L);
        when(properties.getName()).thenReturn("west vloms");
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Arrays.asList(properties));

        Response response = target("/devicetypes/21/deviceconfigurations/22/protocoldialectconfigurationproperties/31").queryParam("fields","id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("west vloms");
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testProtocolDialectConfigurationPropertiesFields() throws Exception {
        Response response = target("/devicetypes/x/deviceconfigurations/x/protocoldialectconfigurationproperties").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "name", "version");
    }


}
