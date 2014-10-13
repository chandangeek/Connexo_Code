package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/13/14.
 */
public class DeviceGroupResourceTest extends DeviceDataRestApplicationJerseyTest {

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
        when(endDeviceGroup.getType()).thenReturn("QueryEndDeviceGroup");
        when(endDeviceGroup.isDynamic()).thenReturn(false);
        List<EndDeviceGroup> endDeviceGroups = Arrays.asList(endDeviceGroup);
        when(restQuery.select(anyObject(), anyObject())).thenReturn(endDeviceGroups);
        when(restQuery2.select(anyObject(), anyObject())).thenReturn(Collections.emptyList());

        String response = target("/devicegroups").queryParam("type", "QueryEndDeviceGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devicegroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.devicegroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<String>get("$.devicegroups[0].type")).isEqualTo("QueryEndDeviceGroups");
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
}
