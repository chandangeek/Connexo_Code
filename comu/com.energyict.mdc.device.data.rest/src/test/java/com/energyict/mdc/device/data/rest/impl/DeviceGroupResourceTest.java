package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.search.SearchDomain;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("South region");
        when(endDeviceGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(queryEndDeviceGroupQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);
        when(endDeviceGroupQuery.select(anyObject(), anyObject())).thenReturn(Collections.emptyList());

        String response = target("/devicegroups").queryParam("type", "QueryEndDeviceGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.devicegroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.devicegroups[0].id")).isEqualTo(13);
    }

    @Test
    public void testGetEndDeviceGroupQueryWithFilter() throws Exception {
        Query<EndDeviceGroup> groupQuery = mock(Query.class);
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(groupQuery);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("South region");
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(groupQuery.select(any(), eq(1), eq(11), any())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").queryParam("filter", ExtjsFilter.filter("name", "South region"))
                .queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
    }

    @Test
    public void testGetEndDeviceGroup() throws Exception {
        Query<EndDeviceGroup> endDeviceGroupQuery = mock(Query.class);
        when(meteringGroupService.getEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(13L);
        when(endDeviceGroup.getName()).thenReturn("south region");
        when(endDeviceGroup.getMRID()).thenReturn("ABC");
        when(endDeviceGroup.getType()).thenReturn("EndDeviceGroup");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(endDeviceGroupQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);

        String response = target("/devicegroups").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
    }

    @Test
    public void testRemoveEndDeviceGroup() {
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(endDeviceGroup));
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(endDeviceGroup).delete();
    }

    @Test
    public void testRemoveNonExistingEndDeviceGroup() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.empty());
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.empty());

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testVetoingHandlerPreventsDeletion() {
        when(meteringGroupService.findAndLockEndDeviceGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(endDeviceGroup));
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));

        DeviceGroupInfo info = new DeviceGroupInfo();
        info.version = 1L;
        info.id = 111L;

        doThrow(new LocalizedException(thesaurus,MessageSeeds.NO_SUCH_MESSAGE) { // Bogus exception, real exception originates in DeviceData.impl
        }).when(endDeviceGroup).delete();

        Response response = target("/devicegroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
    }
    
    @Test
    public void testGetMembersOfStaticDeviceGroup() {
        EnumeratedEndDeviceGroup endDeviceGroup = mock(EnumeratedEndDeviceGroup.class);
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
        assertThat(jsonModel.<List<String>>get("$.devices[*].name")).containsExactly("Name-1", "Name-2");
        assertThat(jsonModel.<List<String>>get("$.devices[*].serialNumber")).containsExactly("001", "002");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceTypeName")).containsExactly("Elster AS1440", "Iskra 001");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceConfigurationName")).containsExactly("Default", "Default");
    }

    @Test
    public void testGetMembersOfDynamicDeviceGroup() {
        QueryEndDeviceGroup queryEndDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(queryEndDeviceGroup));
        when(queryEndDeviceGroup.isDynamic()).thenReturn(true);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));
        Finder<Device> finder = mock(Finder.class);
        doReturn(finder).when(searchDomain).finderFor(Matchers.any());

        List<Device> devices = Arrays.asList(
                mockDevice(1, "001", "Elster AS1440", "Default"),
                mockDevice(2, "002", "Iskra 001", "Default"));
        when(finder.from(Matchers.any())).thenReturn(finder);
        when(finder.find()).thenReturn(devices);
        when(finder.stream()).thenReturn(Stream.of(devices.get(0), devices.get(1)));

        String response = target("/devicegroups/111/devices").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Integer>>get("$.devices[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.devices[*].name")).containsExactly("Name-1", "Name-2");
        assertThat(jsonModel.<List<String>>get("$.devices[*].serialNumber")).containsExactly("001", "002");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceTypeName")).containsExactly("Elster AS1440", "Iskra 001");
        assertThat(jsonModel.<List<String>>get("$.devices[*].deviceConfigurationName")).containsExactly("Default", "Default");
    }
    
    private Device mockDevice(long id, String serialNumber, String deviceType, String deviceConfig) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(id);
        when(device.getName()).thenReturn("Name-" + id);
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
