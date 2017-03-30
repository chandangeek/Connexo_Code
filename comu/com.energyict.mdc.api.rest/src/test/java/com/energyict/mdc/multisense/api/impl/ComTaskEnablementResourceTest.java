/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.tasks.ComTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComTaskEnablementResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetComTaskEnablementsPaged() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(24L);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        ComTask comTask = mockComTask(23, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getId()).thenReturn(102L);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskEnablement.getPriority()).thenReturn(-20);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(24L);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        Response response = target("/devicetypes/21/deviceconfigurations/22/comtaskenablements").queryParam("start",0).queryParam("limit",10).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/comtaskenablements?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(102);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/comtaskenablements/102");
    }

    @Test
    public void testGetSingleComTaskEnablementWithFields() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        ComTask comTask = mockComTask(23, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getId()).thenReturn(102L);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPriority()).thenReturn(-20);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        Response response = target("/devicetypes/21/deviceconfigurations/22/comtaskenablements/102").queryParam("fields","id,priority").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.comTask")).isNull();
    }

    @Test
    public void testGetSingleComTaskEnablementWithoutFields() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(25L);
        when(securityPropertySet.getId()).thenReturn(24L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        ComTask comTask = mockComTask(23, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        PartialScheduledConnectionTask connectionTask = mockPartialOutboundConnectionTask(26, "TCP", deviceConfiguration, 3333L, properties);
        when(comTaskEnablement.getId()).thenReturn(102L);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPriority()).thenReturn(-20);
        when(comTaskEnablement.isSuspended()).thenReturn(true);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(comTaskEnablement.hasPartialConnectionTask()).thenReturn(true);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comTaskEnablement.isSuspended()).thenReturn(true);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        Response response = target("devicetypes/21/deviceconfigurations/22/comtaskenablements/102").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Boolean>get("$.suspended")).isEqualTo(true);
        assertThat(model.<Integer>get("$.comTask.id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTask.link.href")).isEqualTo("http://localhost:9998/comtasks/23");
        assertThat(model.<Integer>get("$.securityPropertySet.id")).isEqualTo(24);
        assertThat(model.<String>get("$.securityPropertySet.link.href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/securitypropertysets/24");
        assertThat(model.<Integer>get("$.partialConnectionTask.id")).isEqualTo(26);
        assertThat(model.<String>get("$.partialConnectionTask.link.href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/partialconnectiontasks/26");
    /*    assertThat(model.<Integer>get("$.protocolDialectConfigurationProperties.id")).isEqualTo(25);       */
    /*    assertThat(model.<String>get("$.protocolDialectConfigurationProperties.link.href")).isEqualTo("http://localhost:9998/devicetypes/21/deviceconfigurations/22/protocoldialectconfigurationproperties/25"); */
    }

    @Test
    public void testComTaskEnablementFields() throws Exception {
        Response response = target("/devicetypes/x/deviceconfigurations/x/comtaskenablements").request("application/json").method("PROPFIND", Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(8);
        assertThat(model.<List<String>>get("$")).containsOnly("id","version","link", "partialConnectionTask", "priority", "securityPropertySet", "comTask", "suspended");
    }


}
