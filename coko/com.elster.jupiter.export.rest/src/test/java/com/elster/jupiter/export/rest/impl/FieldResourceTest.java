/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.RestQuery;

import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldResourceTest extends DataExportApplicationJerseyTest {

    @Test
    public void testGetMeterGroups() {
        Query query = mock(Query.class);
        when(meteringGroupsService.getEndDeviceGroupQuery()).thenReturn(query);
        RestQuery restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        List<EndDeviceGroup> groups = Arrays.asList(mockEndDeviceGroup(1, "EDG1"), mockEndDeviceGroup(2, "EDG2"));
        when(restQuery.select(any(), any())).thenReturn(groups);

        // Business method
        String response = target("/fields/metergroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.metergroups")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.metergroups[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.metergroups[*].name")).containsExactly("EDG1", "EDG2");
    }

    @Test
    public void testGetUsagePointGroups() {
        Query query = mock(Query.class);
        when(meteringGroupsService.getUsagePointGroupQuery()).thenReturn(query);
        RestQuery restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        List<UsagePointGroup> groups = Arrays.asList(mockUsagePointGroup(1, "UPG1"), mockUsagePointGroup(2, "UPG2"));
        when(restQuery.select(any(), any())).thenReturn(groups);

        // Business method
        String response = target("/fields/usagepointgroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.usagePointGroups")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.usagePointGroups[*].id")).containsExactly(1, 2);
        assertThat(jsonModel.<List<String>>get("$.usagePointGroups[*].name")).containsExactly("UPG1", "UPG2");
    }

    private EndDeviceGroup mockEndDeviceGroup(long id, String name) {
        EndDeviceGroup group = mock(EndDeviceGroup.class);
        when(group.getId()).thenReturn(id);
        when(group.getName()).thenReturn(name);
        return group;
    }

    private UsagePointGroup mockUsagePointGroup(long id, String name) {
        UsagePointGroup group = mock(UsagePointGroup.class);
        when(group.getId()).thenReturn(id);
        when(group.getName()).thenReturn(name);
        return group;
    }
}
