/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.RestQuery;

import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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

    @Test
    public void testGetTasksToPairForNewTask() {
        ExportTask et1 = mockExportTask(1, "nameName");
        ExportTask et2 = mockExportTask(7, "nameNama");
        QueryStream exportTasksStream = FakeBuilder.initBuilderStub(Arrays.asList(et2, et1), QueryStream.class, Stream.class);
        doReturn(exportTasksStream).when(dataExportService).streamExportTasks();

        // Business method
        String response = target("/fields/taskstopair").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.tasksToPair")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.tasksToPair[*].id")).containsExactly(7, 1);
        assertThat(jsonModel.<List<String>>get("$.tasksToPair[*].name")).containsExactly("nameNama", "nameName");
    }

    @Test
    public void testGetTasksToPairForUpdatedTask() {
        ExportTask et1 = mockExportTask(1, "nameName");
        ExportTask et2 = mockExportTask(7, "nameNama");
        QueryStream exportTasksStream = FakeBuilder.initBuilderStub(Arrays.asList(et2, et1), QueryStream.class, Stream.class);
        doReturn(exportTasksStream).when(dataExportService).streamExportTasks();
        mockExportTask(2, "namme");

        // Business method
        String response = target("/fields/taskstopair").queryParam("id", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.tasksToPair")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.tasksToPair[*].id")).containsExactly(7, 1);
        assertThat(jsonModel.<List<String>>get("$.tasksToPair[*].name")).containsExactly("nameNama", "nameName");
    }

    @Test
    public void testGetTasksToPairForPairedTask() {
        ExportTask et1 = mockExportTask(1, "nameName");
        ExportTask et2 = mockExportTask(7, "nameNama");
        QueryStream exportTasksStream = FakeBuilder.initBuilderStub(Arrays.asList(et2, et1), QueryStream.class, Stream.class);
        doReturn(exportTasksStream).when(dataExportService).streamExportTasks();
        ExportTask etX = mockExportTask(2, "namme");
        when(etX.getPairedTask()).thenReturn(Optional.of(et2));

        // Business method
        String response = target("/fields/taskstopair").queryParam("id", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.tasksToPair")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.tasksToPair[*].id")).containsExactly(7, 1);
        assertThat(jsonModel.<List<String>>get("$.tasksToPair[*].name")).containsExactly("nameNama", "nameName");
    }

    private ExportTask mockExportTask(long id, String name) {
        ExportTask exportTask = mock(ExportTask.class);
        when(exportTask.getId()).thenReturn(id);
        when(exportTask.getName()).thenReturn(name);
        doReturn(Optional.of(exportTask)).when(dataExportService).findExportTask(id);
        ReadingDataSelectorConfig readingDataSelectorConfig = mock(ReadingDataSelectorConfig.class);
        when(exportTask.getReadingDataSelectorConfig()).thenReturn(Optional.of(readingDataSelectorConfig));
        when(exportTask.getPairedTask()).thenReturn(Optional.empty());
        DataSelectorFactory dataSelectorFactory = mock(DataSelectorFactory.class);
        when(exportTask.getDataSelectorFactory()).thenReturn(dataSelectorFactory);
        return exportTask;
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
