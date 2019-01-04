/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.EndDeviceZoneBuilder;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceZoneResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String ZONE_TYPE_NAME = "ZoneTypeName";
    private static final String ZONE_NAME = "ZoneName";
    private static final String END_DEVICE_NAME = "DeviceName";
    private static final String NO_END_DEVICE_NAME = "NoDeviceName";

    private static final long ZONE_TYPE_ID = 1L;
    private static final long ZONE_ID = 2L;
    private static final long END_DEVICE_ZONE_ID = 3L;
    private static final long NO_END_DEVICE_ZONE_ID = 33L;

    private static final String APPLICATION = "nameOfApplication";
    private static final long VERSION = 1L;

    @Mock
    private EndDeviceZoneBuilder endDeviceZoneBuilder;

    @Before
    public void setUp1() {
        EndDevice endDevice = mock(EndDevice.class);
        when(meteringService.findEndDeviceByName(END_DEVICE_NAME)).thenReturn(Optional.of(endDevice));
        when(meteringService.findEndDeviceByName(NO_END_DEVICE_NAME)).thenReturn(Optional.empty());

        EndDeviceZone endDeviceZone1 = mockEndDeviceZone(ZONE_TYPE_ID, ZONE_TYPE_NAME, ZONE_ID, ZONE_NAME, END_DEVICE_ZONE_ID);
        Finder<EndDeviceZone> endDeviceZoneFinder = mockFinder(Arrays.asList(endDeviceZone1));
        when(meteringZoneService.getByEndDevice(endDevice)).thenReturn(endDeviceZoneFinder);

        when(meteringZoneService.getEndDeviceZone(END_DEVICE_ZONE_ID)).thenReturn(Optional.of(endDeviceZone1));
        when(meteringZoneService.getEndDeviceZone(NO_END_DEVICE_ZONE_ID)).thenReturn(Optional.empty());

        Zone zone = mockZone(ZONE_ID, ZONE_NAME, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME);
        when(meteringZoneService.getZone(ZONE_ID)).thenReturn(Optional.of(zone));
        when(meteringZoneService.newEndDeviceZoneBuilder()).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withEndDevice(endDevice)).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.withZone(zone)).thenReturn(endDeviceZoneBuilder);
        when(endDeviceZoneBuilder.create()).thenReturn(endDeviceZone1);
    }

    private Zone mockZone(Long zoneId, String zoneName, String application, long version, long zoneTypeId, String zoneTypeName) {
        Zone zone = mock(Zone.class);
        when(zone.getId()).thenReturn(zoneId);
        when(zone.getName()).thenReturn(zoneName);
        when(zone.getApplication()).thenReturn(application);
        when(zone.getVersion()).thenReturn(version);
        ZoneType zoneType = mock(ZoneType.class);
        when(zoneType.getId()).thenReturn(zoneTypeId);
        when(zoneType.getName()).thenReturn(zoneTypeName);
        when(zone.getZoneType()).thenReturn(zoneType);
        return zone;
    }

    private EndDeviceZone mockEndDeviceZone(long zoneTypeId, String zoneTypeName, long zoneId, String zoneName, long endDeviceZoneId) {
        ZoneType zoneType = mock(ZoneType.class);
        when(zoneType.getName()).thenReturn(zoneTypeName);
        when(zoneType.getId()).thenReturn(zoneTypeId);

        Zone zone = mock(Zone.class);
        when(zone.getName()).thenReturn(zoneName);
        when(zone.getZoneType()).thenReturn(zoneType);
        when(zone.getId()).thenReturn(zoneId);

        EndDeviceZone endDeviceZone = mock(EndDeviceZone.class);
        when(endDeviceZone.getId()).thenReturn(endDeviceZoneId);
        when(endDeviceZone.getZone()).thenReturn(zone);
        return endDeviceZone;
    }

    @Test
    public void testGetZones() {
        String response = target("/devices/" + END_DEVICE_NAME + "/zones").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.zones[0].zoneTypeName")).isEqualTo(ZONE_TYPE_NAME);
        assertThat(jsonModel.<String>get("$.zones[0].zoneName")).isEqualTo(ZONE_NAME);
    }

    @Test
    public void testGetZonesInvalidNoDeviceFound() throws Exception {
        Response response = target("/devices/" + NO_END_DEVICE_NAME + "/zones").request().get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_DEVICE.getKey());
    }

    @Test
    public void testAddZone() {
        EndDeviceZoneInfo info = new EndDeviceZoneInfo(ZONE_TYPE_NAME, ZONE_NAME, END_DEVICE_ZONE_ID, ZONE_TYPE_ID, ZONE_ID);
        Entity<EndDeviceZoneInfo> json = Entity.json(info);
        Response response = target("/devices/" + END_DEVICE_NAME + "/zones").queryParam("name", END_DEVICE_NAME).request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEditZone() {
        EndDeviceZoneInfo info = new EndDeviceZoneInfo(ZONE_TYPE_NAME, ZONE_NAME, END_DEVICE_ZONE_ID, ZONE_TYPE_ID, ZONE_ID);
        Entity<EndDeviceZoneInfo> json = Entity.json(info);
        Response response = target("/devices/" + END_DEVICE_NAME + "/zones/" + END_DEVICE_ZONE_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteZone() {
        Response response = target("/devices/" + END_DEVICE_NAME + "/zones/" + END_DEVICE_ZONE_ID).request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteInvalidZone() throws Exception {
        Response response = target("/devices/" + END_DEVICE_NAME + "/zones/" + NO_END_DEVICE_ZONE_ID).request().delete();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_END_DEVICE_ZONE.getKey());
    }
}
