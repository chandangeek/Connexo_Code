package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

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
    public void testVetoingHandlerpreventsDeletion() {
        when(meteringGroupService.findEndDeviceGroup(111)).thenReturn(Optional.of(endDeviceGroup));
        doThrow(new RuntimeException("MyMessage")).when(endDeviceGroup).delete();

        try {
            target("/devicegroups/111").request().delete();
        } catch (WebApplicationException e) {
            assertThat(e.getMessage()).isEqualTo("MyMessage");
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

}
