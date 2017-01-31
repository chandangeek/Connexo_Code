/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DeviceMessageEnablementResourceTest extends MultisensePublicApiJerseyTest {

    private DeviceConfiguration deviceConfiguration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        DeviceType deviceType = mockDeviceType(10, "some type", 110);
        deviceConfiguration = mockDeviceConfiguration(11, "some config", deviceType, 111);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceMessageEnablement deviceMessageEnablement = mockDeviceMessageEnablement(31L, deviceConfiguration, DeviceMessageId.ACTIVITY_CALENDAR_READ);
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(deviceMessageEnablement));
    }

    @Test
    public void testAllGetDeviceMessageEnablementsPaged() throws Exception {
        Response response = target("/devicetypes/10/deviceconfigurations/11/devicemessageenablements").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicetypes/10/deviceconfigurations/11/devicemessageenablements?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/10/deviceconfigurations/11/devicemessageenablements/31");
    }

    @Test
    public void testGetSingleDeviceMessageEnablementWithFields() throws Exception {
        Response response = target("/devicetypes/10/deviceconfigurations/11/devicemessageenablements/31").queryParam("fields","id").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<List>get("$.userActions")).isNull();
        assertThat(model.<Integer>get("$.deviceConfiguration.id")).isEqualTo(11);
        assertThat(model.<Integer>get("$.deviceConfiguration.deviceType.id")).isEqualTo(10);
    }

    @Test
    public void testGetSingleDeviceMessageEnablementAllFields() throws Exception {
        Response response = target("/devicetypes/10/deviceconfigurations/11/devicemessageenablements/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/10/deviceconfigurations/11/devicemessageenablements/31");
        assertThat(model.<List>get("$.userActions")).hasSize(1);
        assertThat(model.<String>get("$.userActions[0]")).isEqualTo(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.name());
        assertThat(model.<Integer>get("$.deviceConfiguration.id")).isEqualTo(11);
        assertThat(model.<String>get("$.deviceConfiguration.link.params.rel")).isEqualTo("up");
        assertThat(model.<String>get("$.deviceConfiguration.link.href")).isEqualTo("http://localhost:9998/devicetypes/10/deviceconfigurations/11");
    }

    @Test
    public void testCreateDeviceMessageEnablement() throws Exception {
        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        info.version = 101L;
        info.deviceConfiguration = new DeviceConfigurationInfo();
        info.deviceConfiguration.deviceType = new LinkInfo();
        info.deviceConfiguration.id = 11L;
        info.deviceConfiguration.version = 111L;
        info.deviceConfiguration.deviceType.id = 10L;
        info.deviceConfiguration.deviceType.version = 110L;
        info.messageId = 15001L;
        info.userActions = new HashSet<>();
        info.userActions.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);

        DeviceMessageEnablement deviceMessageEnablement = mockDeviceMessageEnablement(1001L, deviceConfiguration, DeviceMessageId.CLOCK_SET_TIME);
        DeviceMessageEnablementBuilder builder = FakeBuilder.initBuilderStub(deviceMessageEnablement, DeviceMessageEnablementBuilder.class);
        when(deviceConfiguration.createDeviceMessageEnablement(any(DeviceMessageId.class))).thenReturn(builder);
        Response post = target("/devicetypes/10/deviceconfigurations/11/devicemessageenablements/").request().post(Entity.json(info));
        assertThat(post.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCreateDeviceMessageEnablementIncorrectDeviceTypeVersion() throws Exception {
        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        info.version = 101L;
        info.deviceConfiguration = new DeviceConfigurationInfo();
        info.deviceConfiguration.deviceType = new LinkInfo();
        info.deviceConfiguration.id = 11L;
        info.deviceConfiguration.version = 111L;
        info.deviceConfiguration.deviceType.id = 10L;
        info.deviceConfiguration.deviceType.version = 99999999L;
        info.messageId = 15001L;
        info.userActions = new HashSet<>();
        info.userActions.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);

        Response post = target("/devicetypes/10/deviceconfigurations/11/devicemessageenablements/").request().post(Entity.json(info));
        assertThat(post.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeviceMessageEnablementFields() throws Exception {
        Response response = target("/devicetypes/x/deviceconfigurations/x/devicemessageenablements").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(6);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "deviceConfiguration", "userActions", "messageId", "version");
    }
}
