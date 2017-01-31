/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DeviceMessageSpecificationResourceTest extends MultisensePublicApiJerseyTest {

    private DeviceMessageSpec deviceMessageSpec;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        DeviceMessageCategory deviceMessageCategory = mockDeviceMessageCategory(12, "main cat");
        deviceMessageSpec = mockDeviceMessageSpec(DeviceMessageId.CLOCK_SET_TIME, "Set clock");
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageCategory.getMessageSpecifications()).thenReturn(Arrays.asList(deviceMessageSpec));
    }

    @Test
    public void testAllGetDeviceMessageSpecificationsPaged() throws Exception {
        Response response = target("/devicemessagecategories/12/devicemessagespecifications").queryParam("start", 0).queryParam("limit", 10).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicemessagecategories/12/devicemessagespecifications?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(15001);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Set clock");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devicemessagecategories/12/devicemessagespecifications/15001");
    }

    @Test
    public void testGetSingleDeviceMessageSpecificationWithFields() throws Exception {
        Response response = target("/devicemessagecategories/12/devicemessagespecifications/15001").queryParam("fields","id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(15001);
        assertThat(model.<String>get("$.name")).isEqualTo("Set clock");
        assertThat(model.<Object>get("$.link")).isNull();
        assertThat(model.<String>get("$.category")).isNull();
        assertThat(model.<String>get("$.deviceMessageId")).isNull();
        assertThat(model.<List>get("$.propertySpecs")).isNull();
    }

    @Test
    public void testGetSingleDeviceMessageSpecificationFull() throws Exception {
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        when(deviceMessageSpec.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicemessagecategories/12/devicemessagespecifications/15001").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(15001);
        assertThat(model.<String>get("$.name")).isEqualTo("Set clock");
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicemessagecategories/12/devicemessagespecifications/15001");
        assertThat(model.<Integer>get("$.category.id")).isEqualTo(12);
        assertThat(model.<String>get("$.category.link.href")).isEqualTo("http://localhost:9998/devicemessagecategories/12");
        assertThat(model.<String>get("$.deviceMessageId")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<List>get("$.propertySpecs")).hasSize(1);
    }



    @Test
    public void testDeviceMessageSpecificationFields() throws Exception {
        Response response = target("/devicemessagecategories/x/devicemessagespecifications").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(6);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "name", "deviceMessageId", "propertySpecs", "category");
    }


}
