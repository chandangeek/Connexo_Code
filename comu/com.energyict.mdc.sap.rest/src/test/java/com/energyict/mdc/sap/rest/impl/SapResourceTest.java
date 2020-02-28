/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SapResourceTest extends SapApplicationJerseyTest {

    @Mock
    EndPointConfiguration endPointConfiguration;

    @Mock
    Device device;

    @Test
    public void testHasSapCas() throws IOException {

        //no such device
        when(deviceService.findDeviceById(1)).thenReturn(Optional.empty());

        Response response = target("/devices/1/hassapcas").request().get();
        assertBadRequest(response, "No device with id '1'.", "NoSuchDevice");

        //device hasn't sap cps
        when(deviceService.findDeviceById(1)).thenReturn(Optional.of(device));
        when(sapCustomPropertySets.doesDeviceHaveSapCPS(device)).thenReturn(false);

        Boolean booleanResponse = target("/devices/1/hassapcas").request().get(Boolean.class);
        assertThat(booleanResponse).isFalse();

        //device has sap cps
        when(sapCustomPropertySets.doesDeviceHaveSapCPS(device)).thenReturn(true);

        booleanResponse = target("/devices/1/hassapcas").request().get(Boolean.class);
        assertThat(booleanResponse).isTrue();
    }

    @Test
    public void testGetAvailableRegisteredNotificationEndpoints() {

        //empty case
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME))
                .thenReturn(Collections.emptyList());

        String stringResponse = target("/registerednotificationendpoints").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.registeredNotificationEndpoints")).isNotNull();
        assertThat(model.<List<?>>get("$.registeredNotificationEndpoints")).isEmpty();

        //one registered notification endpoint
        mockRegisteredNotificationEndPoint();

        stringResponse = target("/registerednotificationendpoints").request().get(String.class);
        model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.registeredNotificationEndpoints")).isNotNull();
        assertThat(model.<List<?>>get("$.registeredNotificationEndpoints")).hasSize(1);
        assertThat(model.<Number>get("$.registeredNotificationEndpoints[0].id")).isEqualTo(10);
        assertThat(model.<String>get("$.registeredNotificationEndpoints[0].name")).isEqualTo("TestSmartMeterRegisteredNotification");
        assertThat(model.<Number>get("$.registeredNotificationEndpoints[0].version")).isEqualTo(1);
    }

    @Test
    public void testSendRegisteredNotification() throws IOException {
        Instant now = Instant.now();
        when(this.clock.instant()).thenReturn(now);

        RegisteredNotificationEndPointInfo info = new RegisteredNotificationEndPointInfo();
        info.id = 10;
        info.name = "TestSmartMeterRegisteredNotification";
        info.version = 1;

        Entity<RegisteredNotificationEndPointInfo> entity = Entity.json(info);

        //no registered notification end points
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME))
                .thenReturn(Collections.emptyList());

        Response response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertBadRequest(response, "No registered notification end point is found by id '10'.", "NoRegisteredNotificationEndPoint");

        //no such device
        when(deviceService.findDeviceById(1)).thenReturn(Optional.empty());
        mockRegisteredNotificationEndPoint();

        response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertBadRequest(response, "No device with id '1'.", "NoSuchDevice");

        //device id attribute is not set
        when(deviceService.findDeviceById(1)).thenReturn(Optional.of(device));
        when(sapCustomPropertySets.getSapDeviceId(device)).thenReturn(Optional.empty());
        response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertBadRequest(response, "'Device identifier' attribute is not set on Device SAP info CAS.", "DeviceIdAttributeIsNotSet");

        //device already registered
        when(sapCustomPropertySets.getSapDeviceId(device)).thenReturn(Optional.of("SAP10001"));
        when(sapCustomPropertySets.isRegistered(device)).thenReturn(true);
        response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertBadRequest(response, "Device already registered (Registered flag is true on Device SAP info CAS).", "DeviceAlreadyRegistered");

        //no lrn
        when(sapCustomPropertySets.isRegistered(device)).thenReturn(false);
        when(sapCustomPropertySets.isAnyLrnPresent(1, now)).thenReturn(false);
        response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertBadRequest(response, "No LRN is available on current or future data sources on the device.", "NoLrn");

        //registered notification is sent
        when(sapCustomPropertySets.isAnyLrnPresent(1, now)).thenReturn(true);
        response = target("/devices/1/sendregisterednotification").request().post(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(utilitiesDeviceRegisteredNotification).call("SAP10001", Collections.singletonList(endPointConfiguration));
    }

    private void mockRegisteredNotificationEndPoint() {
        when(endPointConfiguration.getId()).thenReturn(10L);
        when(endPointConfiguration.getName()).thenReturn("TestSmartMeterRegisteredNotification");
        when(endPointConfiguration.getWebServiceName()).thenReturn(UtilitiesDeviceRegisteredNotification.NAME);
        when(endPointConfiguration.getVersion()).thenReturn(1L);
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME))
                .thenReturn(Collections.singletonList(endPointConfiguration));
    }
}
