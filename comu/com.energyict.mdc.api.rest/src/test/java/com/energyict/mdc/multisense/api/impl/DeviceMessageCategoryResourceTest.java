/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testGetAllDeviceMessageCategoriesPaged() throws Exception {
        DeviceMessageCategory category2 = mockDeviceMessageCategory(14, "All");
        when(deviceMessageSpecificationService.allCategories()).thenReturn(Arrays.asList(category2));
        Response response = target("/devicemessagecategories").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicemessagecategories?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(14);
        assertThat(model.<String>get("data[0].name")).isEqualTo("All");
        assertThat(model.<String>get("data[0].description")).isEqualTo("Description of All");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devicemessagecategories/14");

    }

    @Test
    public void testGetDeviceMessageCategory() throws Exception {
        DeviceMessageCategory category2 = mockDeviceMessageCategory(13, "All");
        Response response = target("/devicemessagecategories/13").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(13);
        assertThat(model.<String>get("$.name")).isEqualTo("All");
        assertThat(model.<String>get("$.description")).isEqualTo("Description of All");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicemessagecategories/13");
    }

    @Test
    public void testGetDeviceMessageCategoryWithFieldSelection() throws Exception {
        DeviceMessageCategory category2 = mockDeviceMessageCategory(13, "All");
        Response response = target("/devicemessagecategories/13").queryParam("fields", "id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(13);
        assertThat(model.<String>get("$.name")).isEqualTo("All");
        assertThat(model.<String>get("$.description")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testDeviceMessageCategoryFields() throws Exception {
        Response response = target("/devicemessagecategories").request(MediaType.APPLICATION_JSON_TYPE).method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(5);
        assertThat(model.<List<String>>get("$")).containsOnly("description", "id", "link", "name", "deviceMessageSpecs");
    }


}
