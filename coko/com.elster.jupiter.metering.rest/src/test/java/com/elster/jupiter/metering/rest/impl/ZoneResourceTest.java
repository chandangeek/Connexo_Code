/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.rest.impl.zone.ZoneInfo;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneBuilder;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.metering.zone.ZoneTypeBuilder;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZoneResourceTest extends MeteringApplicationJerseyTest {

    private static final String ZONE_NAME = "ZoneName";
    private static final long ZONE_ID = 1L;
    private static final String APPLICATION = "nameOfApplication";
    private static final String ZONE_TYPE_NAME = "ZoneTypeName";
    private static final long ZONE_TYPE_ID = 10L;
    private static final long VERSION = 1L;

    private static final long ZONE_ID_2 = 2L;

    private static final String ZONE_NAME_ABC = "ABC";
    private static final String ZONE_NAME_BCD = "BCD";
    private static final String ZONE_TYPE_NAME_ABC = "abc";
    private static final String ZONE_TYPE_NAME_BCD = "bcd";
    private static final String ZONE_TYPE_NAME_CDE = "cde";

    @Mock
    private ZoneBuilder zoneBuilder;
    @Mock
    private ZoneTypeBuilder zoneTypeBuilder;

    @Before
    public void setUp1() {
        Zone zone = mockZone(ZONE_ID, ZONE_NAME, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME);
        when(meteringZoneService.getZone(ZONE_ID)).thenReturn(Optional.of(zone));

        when(meteringZoneService.getZone(ZONE_ID_2)).thenReturn(Optional.empty());

        Zone zone1 = mockZone(ZONE_ID, ZONE_NAME_BCD, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME_CDE);
        Zone zone2 = mockZone(ZONE_ID, ZONE_NAME_ABC, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME_ABC);
        Zone zone3 = mockZone(ZONE_ID, ZONE_NAME_ABC, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME_BCD);
        Finder<Zone> zoneFinder = mockFinder(Arrays.asList(zone1, zone2, zone3));
        when(meteringZoneService.getZones(any(String.class), any())).thenReturn(zoneFinder);

        ZoneType zoneType1 = mockZoneType(ZONE_TYPE_ID, ZONE_TYPE_NAME_CDE);
        ZoneType zoneType2 = mockZoneType(ZONE_TYPE_ID, ZONE_TYPE_NAME_ABC);
        when(meteringZoneService.getZoneTypes(any(String.class))).thenReturn(Arrays.asList(zoneType1, zoneType2));

        Zone newZone = mockZone(ZONE_ID, ZONE_NAME, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME);
        when(meteringZoneService.newZoneBuilder()).thenReturn(zoneBuilder);
        when(zoneBuilder.withName(any(String.class))).thenReturn(zoneBuilder);
        when(zoneBuilder.withZoneType(any(ZoneType.class))).thenReturn(zoneBuilder);
        when(zoneBuilder.create()).thenReturn(newZone);
        when(meteringZoneService.getZoneType(any(), any())).thenReturn(Optional.of(zoneType1));

        when(meteringZoneService.newZoneTypeBuilder()).thenReturn(zoneTypeBuilder);
        when(zoneTypeBuilder.withName(any(String.class))).thenReturn(zoneTypeBuilder);
        when(zoneTypeBuilder.withApplication(any(String.class))).thenReturn(zoneTypeBuilder);

        Zone updateZone = mockZone(ZONE_ID, ZONE_NAME, APPLICATION, VERSION, ZONE_TYPE_ID, ZONE_TYPE_NAME);
        when(meteringZoneService.getAndLockZone(ZONE_ID, VERSION)).thenReturn(Optional.of(updateZone));
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

    private ZoneType mockZoneType(long zoneTypeId, String zoneTypeName) {
        ZoneType zoneType = mock(ZoneType.class);
        when(zoneType.getId()).thenReturn(zoneTypeId);
        when(zoneType.getName()).thenReturn(zoneTypeName);
        return zoneType;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(com.elster.jupiter.domain.util.QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    @Test
    public void testGetZones() {
        String json = target("zones").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<String>get("$.zones[0].name")).isEqualTo(ZONE_NAME_ABC);
        assertThat(jsonModel.<String>get("$.zones[0].zoneTypeName")).isEqualTo(ZONE_TYPE_NAME_ABC);
        assertThat(jsonModel.<String>get("$.zones[1].name")).isEqualTo(ZONE_NAME_ABC);
        assertThat(jsonModel.<String>get("$.zones[1].zoneTypeName")).isEqualTo(ZONE_TYPE_NAME_BCD);
        assertThat(jsonModel.<String>get("$.zones[2].name")).isEqualTo(ZONE_NAME_BCD);
        assertThat(jsonModel.<String>get("$.zones[2].zoneTypeName")).isEqualTo(ZONE_TYPE_NAME_CDE);
    }

    @Test
    public void testGetZone() {
        ZoneInfo response = target("zones/" + ZONE_ID).request().get(ZoneInfo.class);
        assertThat(response.id).isEqualTo(ZONE_ID);
        assertThat(response.application).isEqualTo(APPLICATION);
        assertThat(response.name).isEqualTo(ZONE_NAME);
        assertThat(response.zoneTypeId).isEqualTo(ZONE_TYPE_ID);
        assertThat(response.zoneTypeName).isEqualTo(ZONE_TYPE_NAME);
        assertThat(response.version).isEqualTo(VERSION);
    }

    @Test
    public void testGetNotFoundZone() {
        Response response = target("zones/" + ZONE_ID_2).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetZoneTypes() {
        String json = target("zones/types").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.types[0].name")).isEqualTo(ZONE_TYPE_NAME_ABC);
        assertThat(jsonModel.<String>get("$.types[1].name")).isEqualTo(ZONE_TYPE_NAME_CDE);
    }

    @Test
    public void testCreateZone() {
        ZoneInfo info = new ZoneInfo(0L, ZONE_NAME, APPLICATION, 0L, ZONE_TYPE_NAME, 0L);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCreateZoneWithoutName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, "", APPLICATION, ZONE_TYPE_ID, ZONE_TYPE_NAME, VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testCreateZoneWithInexistentTypeName() throws Exception {
        when(meteringZoneService.getZoneType(any(), any())).thenReturn(Optional.empty());
        ZoneInfo info = new ZoneInfo(0L, ZONE_NAME, APPLICATION, ZONE_TYPE_ID, "inexistentType", VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCreateZoneWithoutTypeName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, ZONE_NAME, APPLICATION, ZONE_TYPE_ID, "", VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("zoneTypeName");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testCreateZoneWithoutNameAndTypeName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, "", APPLICATION, ZONE_TYPE_ID, "", VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
        assertThat(jsonModel.<String>get("$.errors[1].id")).isEqualTo("zoneTypeName");
        assertThat(jsonModel.<String>get("$.errors[1].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testUpdateZone() {
        ZoneInfo info = new ZoneInfo(0L, ZONE_NAME, APPLICATION, 0L, ZONE_TYPE_NAME, VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones/" + ZONE_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateZoneWithoutName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, "", APPLICATION, ZONE_TYPE_ID, ZONE_TYPE_NAME, VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones/" + ZONE_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testUpdateZoneWithoutTypeName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, ZONE_NAME, APPLICATION, ZONE_TYPE_ID, "", VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones/" + ZONE_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("zoneTypeName");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testUpdateZoneWithoutNameAndTypeName() throws Exception {
        ZoneInfo info = new ZoneInfo(0L, "", APPLICATION, ZONE_TYPE_ID, "", VERSION);

        Entity<ZoneInfo> json = Entity.json(info);
        Response response = target("/zones/" + ZONE_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
        assertThat(jsonModel.<String>get("$.errors[1].id")).isEqualTo("zoneTypeName");
        assertThat(jsonModel.<String>get("$.errors[1].msg")).isEqualTo("This field is required");
    }

    @Test
    public void testDeleteZone() throws Exception {
        Response response = target("/zones/" + ZONE_ID).request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteInvalidZone() throws Exception {
        Response response = target("/zones/" + ZONE_ID_2).request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
