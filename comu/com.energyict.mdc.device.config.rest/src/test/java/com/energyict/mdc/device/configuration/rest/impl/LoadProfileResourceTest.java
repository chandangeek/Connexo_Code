package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;

import com.elster.jupiter.time.TimeDuration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LoadProfileResourceTest extends BaseLoadProfileTest {

    @Test
    public void testGetLoadProfilesForDeviceType(){
        DeviceType deviceType = mockDeviceType("device", 1);
        List<LoadProfileType> loadProfiles = getLoadProfileTypes(3);
        when(deviceType.getLoadProfileTypes()).thenReturn(loadProfiles);
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/1/loadprofiletypes").queryParam("available", false).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(3);
        assertThat((List)map.get("data")).hasSize(3);
    }


    @Test
    public void testGetAvailableLoadProfilesForDeviceType(){
        List<LoadProfileType> allLoadProfiles = getLoadProfileTypes(15);
        List<LoadProfileType> assignedToDeviceType = new ArrayList<>(3);
        assignedToDeviceType.add(allLoadProfiles.get(2));
        assignedToDeviceType.add(allLoadProfiles.get(7));
        assignedToDeviceType.add(allLoadProfiles.get(12));
        DeviceType deviceType = mockDeviceType("device", 1);

        when(masterDataService.findAllLoadProfileTypes()).thenReturn(allLoadProfiles);
        when(deviceType.getLoadProfileTypes()).thenReturn(assignedToDeviceType);
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/1/loadprofiletypes").queryParam("available", true).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(12);
        List<?> data = (List)map.get("data");
        assertThat(data).hasSize(12);
        assertThat(((Map)data.get(0)).get("name")).isEqualTo(allLoadProfiles.get(0).getName());
    }

    @Test
    public void testDeleteLoadProfileTypeFromDeviceType(){
        DeviceType deviceType = mockDeviceType("device", 2);
        TimeDuration interval = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(2, "name", interval, new ObisCode(0, 1, 2, 3, 4, 5), getChannelTypes(1, interval));
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceType(2)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.empty());
        when(masterDataService.findLoadProfileType(2)).thenReturn(Optional.of(loadProfileType));

        Response response = target("/devicetypes/1/loadprofiletypes/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        response = target("/devicetypes/2/loadprofiletypes/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        response = target("/devicetypes/2/loadprofiletypes/2").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAddLoadProfileTypesForDeviceType(){
        DeviceType deviceType = mockDeviceType("device", 1);
        List<Integer> ids = new ArrayList<>();
        Entity<List<Integer>> json = Entity.json(ids);
        TimeDuration interval = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(1, "name", interval, new ObisCode(0, 1, 2, 3, 4, 5), getChannelTypes(1, interval));

        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.of(loadProfileType));
        when(masterDataService.findLoadProfileType(9999)).thenReturn(Optional.empty());

        Response response = target("/devicetypes/1/loadprofiletypes").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        ids.add(1);

        response = target("/devicetypes/1/loadprofiletypes").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ids.add(9999);
        response = target("/devicetypes/1/loadprofiletypes").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
