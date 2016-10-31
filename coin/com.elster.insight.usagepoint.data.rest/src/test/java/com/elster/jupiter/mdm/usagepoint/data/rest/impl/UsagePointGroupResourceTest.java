package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.StatusCode;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointGroupResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Mock
    private UsagePointGroup usagePointGroup;

    @Test
    public void testGetQueryUsagePointGroup() throws Exception {
        Query<UsagePointGroup> queryUsagePointGroupQuery = mock(Query.class);
        when(meteringGroupsService.getQueryUsagePointGroupQuery()).thenReturn(queryUsagePointGroupQuery);
        Query<UsagePointGroup> usagePointGroupQuery = mock(Query.class);
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        when(usagePointGroup.getMRID()).thenReturn("LAPOPKLQKS");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(queryUsagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(usagePointGroups);
        when(usagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(Collections.emptyList());

        String response = target("/usagepointgroups").queryParam("type", "QueryUsagePointGroup").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].name")).isEqualTo("South region");
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].mRID")).isEqualTo("LAPOPKLQKS");
        assertThat(jsonModel.<Integer>get("$.usagepointgroups[0].id")).isEqualTo(13);
    }

    @Test
    public void testGetUsagePointGroupQueryWithFilter() throws Exception {
        Query<UsagePointGroup> groupQuery = mock(Query.class);
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(groupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("South region");
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(groupQuery.select(any(), eq(1), eq(11), any())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").queryParam("filter", ExtjsFilter.filter("name", "South region"))
                .queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.usagepointgroups[0].name")).isEqualTo("South region");
    }

    @Test
    public void testGetUsagePointGroup() throws Exception {
        Query<UsagePointGroup> usagePointGroupQuery = mock(Query.class);
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(13L);
        when(usagePointGroup.getName()).thenReturn("south region");
        when(usagePointGroup.getMRID()).thenReturn("ABC");
        when(usagePointGroup.getType()).thenReturn("UsagePointGroup");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        List<UsagePointGroup> usagePointGroups = Collections.singletonList(usagePointGroup);
        when(usagePointGroupQuery.select(anyObject(), anyObject())).thenReturn(usagePointGroups);

        String response = target("/usagepointgroups").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
    }

    @Test
    public void testRemoveUsagePointGroup() {
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(usagePointGroup));
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(usagePointGroup).delete();
    }

    @Test
    public void testRemoveNonExistingUsagePointGroup() {
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.empty());
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.empty());

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testVetoingHandlerPreventsDeletion() {
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(111, 1L)).thenReturn(Optional.of(usagePointGroup));
        when(meteringGroupsService.findUsagePointGroup(111)).thenReturn(Optional.of(usagePointGroup));

        UsagePointGroupInfo info = new UsagePointGroupInfo();
        info.version = 1L;
        info.id = 111L;

        doThrow(new LocalizedException(thesaurus, MessageSeeds.BAD_REQUEST) {
            // Bogus exception, real exception originates in subscribers
            // of com.elster.jupiter.metering.groups.EventType.USAGEPOINTGROUP_VALIDATE_DELETED
        }).when(usagePointGroup).delete();

        Response response = target("/usagepointgroups/111").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
    }

}
