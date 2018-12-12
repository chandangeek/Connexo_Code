/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;

import com.jayway.jsonpath.JsonModel;

import java.util.List;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class EndDeviceEventTypeResourceTest extends MeteringApplicationJerseyTest {

    @Test
    public void testGetDeviceTypes(){
        String response = target("/enddeviceeventtypes/devicetypes").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        int total = EndDeviceType.values().length;
        assertThat(model.<Number>get("$.total")).isEqualTo(total);
        assertThat(model.<List>get("$.endDeviceEventTypePartInfos")).hasSize(total);
    }

    @Test
    public void testGetDomains(){
        String response = target("/enddeviceeventtypes/devicedomains").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        int total = EndDeviceDomain.values().length;
        assertThat(model.<Number>get("$.total")).isEqualTo(total);
        assertThat(model.<List>get("$.endDeviceEventTypePartInfos")).hasSize(total);
    }

    @Test
    public void testGetSubDomains(){
        String response = target("/enddeviceeventtypes/devicesubdomains").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        int total = EndDeviceSubDomain.values().length;
        assertThat(model.<Number>get("$.total")).isEqualTo(total);
        assertThat(model.<List>get("$.endDeviceEventTypePartInfos")).hasSize(total);
    }

    @Test
    public void testGetEventOrActions(){
        String response = target("/enddeviceeventtypes/deviceeventoractions").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        int total = EndDeviceEventOrAction.values().length;
        assertThat(model.<Number>get("$.total")).isEqualTo(total);
        assertThat(model.<List>get("$.endDeviceEventTypePartInfos")).hasSize(total);
    }
}
