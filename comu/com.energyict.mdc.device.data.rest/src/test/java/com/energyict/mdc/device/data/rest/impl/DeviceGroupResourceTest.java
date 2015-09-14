package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.jayway.jsonpath.JsonModel;

/**
 * Created by bvn on 10/13/14.
 */
public class DeviceGroupResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    private EndDeviceGroup endDeviceGroup;

    @Test
    public void testGetQueryEndDeviceGroup() throws Exception {
        Query<EndDeviceGroup> queryEndDeviceGroupQuery = mock(Query.class);
        when(meteringGroupService.getQueryEndDeviceGroupQuery()).thenReturn(queryEndDeviceGroupQuery);
        Query<EndDeviceGroup> endDeviceGroupQuery = mock(Query.class);
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        RestQuery<EndDeviceGroup> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(queryEndDeviceGroupQuery)).thenReturn(restQuery);
        RestQuery<EndDeviceGroup> restQuery2 = mock(RestQuery.class);
        when(restQueryService.wrap(endDeviceGroupQuery)).thenReturn(restQuery2);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("South region");
        when(endDeviceGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(restQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);
        when(restQuery2.select(anyObject(), anyObject())).thenReturn(Collections.emptyList());

        String response = target("/devicegroups").queryParam("type", "QueryEndDeviceGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.devicegroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.devicegroups[0].id")).isEqualTo(13);
    }

    @Test
    public void testGetEndDeviceGroup() throws Exception {
        Query<EndDeviceGroup> endDeviceGroupQuery = mock(Query.class);
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        RestQuery<EndDeviceGroup> restQuery2 = mock(RestQuery.class);
        when(restQueryService.wrap(endDeviceGroupQuery)).thenReturn(restQuery2);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("south region");
        when(endDeviceGroup.getMRID()).thenReturn("ABC");
        when(endDeviceGroup.getType()).thenReturn("EndDeviceGroup");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(restQuery2.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
    }

    @Test
    public void testRemoveEndDeviceGroup() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));

        target("/devicegroups/111").request().delete();

        verify(endDeviceGroup).delete();
    }

    @Test
    public void testRemoveNonExistingEndDeviceGroup() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.empty());

        try {
            target("/devicegroups/111").request().delete();
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND);
        }

    }

    @Test
    public void testVetoingHandlerPreventsDeletion() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));
        doThrow(new LocalizedException(thesaurus,MessageSeeds.NO_SUCH_MESSAGE) { // Bogus exception, real exception originates in DeviceData.impl
        }).when(endDeviceGroup).delete();

        Response response = target("/devicegroups/111").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void testGetMembersOfDeviceGroup() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));
        EndDevice endDevice = mock(EndDevice.class);
        when(endDevice.getAmrId()).thenReturn("1");
        when(endDeviceGroup.getMembers(Matchers.any(Instant.class))).thenReturn(Arrays.asList(endDevice));
        Finder<Device> finder = mock(Finder.class);
        when(deviceService.findAllDevices(Matchers.any())).thenReturn(finder);
        when(finder.sorted("mRID", true)).thenReturn(finder);
        List<Device> devices = Arrays.asList(
                mockDevice(1, "001", "Elster AS1440", "Default"),
                mockDevice(2, "002", "Iskra 001", "Default"));
        when(finder.find()).thenReturn(devices);
        when(finder.stream()).thenReturn(Stream.of(devices.get(0), devices.get(1)));

        String response = target("/devicegroups/111/devices").request().get(String.class);
        
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.devices[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.devices[*].mRID")).containsExactly("MRID1", "MRID2");
        assertThat(jsonModel.<List<String>>get("$.devices[*].serialNumber")).containsExactly("001", "002");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceTypeName")).containsExactly("Elster AS1440", "Iskra 001");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceConfigurationName")).containsExactly("Default", "Default");
    }
    
    private Device mockDevice(long id, String serialNumber, String deviceType, String deviceConfig) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(id);
        when(device.getmRID()).thenReturn("MRID" + id);
        when(device.getSerialNumber()).thenReturn(serialNumber);
        DeviceType type = mock(DeviceType.class);
        when(device.getDeviceType()).thenReturn(type);
        when(type.getName()).thenReturn(deviceType);
        DeviceConfiguration config = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(config);
        when(config.getName()).thenReturn(deviceConfig);
        return device;
    }

}
