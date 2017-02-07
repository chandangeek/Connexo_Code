/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.LoadProfileType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoadProfileConfigurationResourceTest extends BaseLoadProfileTest {
    @Test
    public void testGetLoadProfileSpecsForDeviceConfiguration(){
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);
        List<LoadProfileSpec> loadProfileSpecs = getLoadProfileSpecs(5);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileSpecs);
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            when(loadProfileSpec.getChannelSpecs()).thenReturn(Collections.emptyList());
            when(loadProfileSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        }

        Map<String, Object> map = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(5);
        assertThat((List)map.get("data")).hasSize(5);
    }

    @Test
    public void testGetAvailableLoadProfileSpecsForDeviceConfiguration(){
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        List<LoadProfileSpec> loadProfileSpecs = getLoadProfileSpecs(5);
        List<LoadProfileType> loadProfileTypes = getLoadProfileTypes(10);

        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceType.getLoadProfileTypes()).thenReturn(loadProfileTypes);
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileSpecs);
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            when(loadProfileSpec.getChannelSpecs()).thenReturn(Collections.emptyList());
        }

        Map<String, Object> map = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/available").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(5);
        assertThat((List)map.get("data")).hasSize(5);
    }

    @Test
    public void testGetUnexistedLoadProfileSpec() {
        when(deviceConfigurationService.findLoadProfileSpec(anyLong())).thenReturn(Optional.empty());

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLoadProfileSpec(){
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1L);
        when(loadProfileSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfigurationService.findLoadProfileSpec(1L)).thenReturn(Optional.of(loadProfileSpec));

        Map<String, Object> map = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().get(Map.class);
        assertThat(map.get("id")).isEqualTo(1);
        assertThat(map.get("name")).isEqualTo("Load profile spec 1");
        assertThat(map.get("version")).isEqualTo(((Number)OK_VERSION).intValue());
    }

    @Test
    public void testDeleteLoadProfileSpecFromDeviceConfiguration(){
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1);
        when(loadProfileSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(1L, OK_VERSION);
        Entity<LoadProfileSpecInfo> json = Entity.json(info);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAddLoadProfileTypesForDeviceType(){
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);

        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        Entity<LoadProfileSpecInfo> json = Entity.json(info);
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(1, "loadProfile", interval, new ObisCode(0, 1, 2, 3, 4, 5), getChannelTypes(1, interval));
        LoadProfileSpec.LoadProfileSpecBuilder specBuilder = mock(LoadProfileSpec.LoadProfileSpecBuilder.class);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(15);
        when(loadProfileSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.of(loadProfileType));
        when(deviceConfiguration.createLoadProfileSpec(loadProfileType)).thenReturn(specBuilder);
        when(specBuilder.add()).thenReturn(loadProfileSpec);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());

        info.id = 1;
        info.overruledObisCode = new ObisCode(200,201,202,203,204,205);
        response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEditLoadProfileSpecOnDeviceConfigurationBadVersion() {
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1);

        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.overruledObisCode = new ObisCode(200, 201, 202, 203, 204, 205);
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(1L, BAD_VERSION);
        Entity<LoadProfileSpecInfo> json = Entity.json(info);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testEditLoadProfileSpecOnDeviceConfiguration(){
        ObisCode obisCode = new ObisCode(200, 201, 202, 203, 204, 205);

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1L);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1);
        when(loadProfileSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = mock(LoadProfileSpec.LoadProfileSpecUpdater.class);
        when(specUpdater.setOverruledObisCode(obisCode)).thenReturn(specUpdater);
        when(deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec)).thenReturn(specUpdater);

        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.overruledObisCode = obisCode;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(1L, OK_VERSION);
        Entity<LoadProfileSpecInfo> json = Entity.json(info);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getAvailableMeasurementTypesForChannelTest() {

    }
}
