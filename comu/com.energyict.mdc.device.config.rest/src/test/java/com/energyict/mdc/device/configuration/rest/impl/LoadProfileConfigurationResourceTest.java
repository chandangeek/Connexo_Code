package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.time.TimeDuration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoadProfileConfigurationResourceTest extends BaseLoadProfileTest {

    private DeviceConfiguration getDeviceConfiguration() {
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 1);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        return deviceConfiguration;
    }

    @Test
    public void testGetLoadProfileSpecsForDeviceConfiguration(){
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        List<LoadProfileSpec> loadProfileSpecs = getLoadProfileSpecs(5);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileSpecs);
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            when(loadProfileSpec.getChannelSpecs()).thenReturn(Collections.emptyList());
        }

        Map<String, Object> map = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(5);
        assertThat((List)map.get("data")).hasSize(5);
    }

    @Test
    public void testGetAvailableLoadProfileSpecsForDeviceConfiguration(){
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 1);
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
    public void testGetLoadProfileSpec(){
        when(deviceConfigurationService.findLoadProfileSpec(anyLong())).thenReturn(Optional.empty());

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1L, "spec");
        when(deviceConfigurationService.findLoadProfileSpec(1L)).thenReturn(Optional.of(loadProfileSpec));

        Map<String, Object> map = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        map = (Map<String, Object>) ((List)map.get("data")).get(0);
        assertThat(map.get("id")).isEqualTo(1);
        assertThat(map.get("name")).isEqualTo("spec");
    }

    @Test
    public void testDeleteLoadProfileSpecFromDeviceConfiguration(){
        getDeviceConfiguration();

        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1, "spec");
        when(deviceConfigurationService.findLoadProfileSpec(anyLong())).thenReturn(Optional.empty());

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/9999").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        when(deviceConfigurationService.findLoadProfileSpec(1L)).thenReturn(Optional.of(loadProfileSpec));

        response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAddLoadProfileTypesForDeviceType(){
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();

        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        Entity<LoadProfileSpecInfo> json = Entity.json(info);
        TimeDuration interval = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(1, "loadProfile", interval, new ObisCode(0, 1, 2, 3, 4, 5), getChannelTypes(1, interval));
        LoadProfileSpec.LoadProfileSpecBuilder specBuilder = mock(LoadProfileSpec.LoadProfileSpecBuilder.class);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(15, "spec");

        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.of(loadProfileType));
        when(deviceConfiguration.createLoadProfileSpec(loadProfileType)).thenReturn(specBuilder);
        when(specBuilder.add()).thenReturn(loadProfileSpec);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        info.id = 1;
        info.overruledObisCode = new ObisCode(200,201,202,203,204,205);
        response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEditLoadProfileSpecOnDeviceConfiguration(){
        DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.overruledObisCode = new ObisCode(200,201,202,203,204,205);
        Entity<LoadProfileSpecInfo> json = Entity.json(info);
        LoadProfileSpec loadProfileSpec = mockLoadProfileSpec(1, "spec");
        when(deviceConfigurationService.findLoadProfileSpec(1L)).thenReturn(Optional.empty());
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = mock(LoadProfileSpec.LoadProfileSpecUpdater.class);

        Response response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        when(deviceConfigurationService.findLoadProfileSpec(1L)).thenReturn(Optional.of(loadProfileSpec));
        when(specUpdater.setOverruledObisCode(info.overruledObisCode)).thenReturn(specUpdater);
        when(deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec)).thenReturn(specUpdater);

        response = target("/devicetypes/1/deviceconfigurations/1/loadprofileconfigurations/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
