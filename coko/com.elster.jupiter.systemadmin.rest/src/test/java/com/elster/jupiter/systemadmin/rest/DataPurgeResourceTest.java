/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.systemadmin.rest.imp.response.LifeCycleCategoryInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ListInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.PurgeHistoryInfo;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataPurgeResourceTest extends SystemApplicationJerseyTest {

    private static final Instant JUN_2014 = LocalDateTime.of(2014, 6, 1, 10, 0, 0).toInstant(ZoneOffset.UTC);
    private static final long OK_VERSION = 17L;
    private static final long BAD_VERSION = 16L;

    @Test
    public void testLifeCycleCategoryInfoModel(){
        LifeCycleCategory category = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 10);

        when(lifeCycleService.getCategories()).thenReturn(Collections.singletonList(category));
        String response = target("/data/lifecycle/categories").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number> get("$.total")).isEqualTo(1);
        assertThat(model.<List> get("$.data").size()).isEqualTo(1);
        assertThat(model.<String> get("$.data[0].kind")).isEqualTo("REGISTER");
        assertThat(model.<String> get("$.data[0].name")).isNotEmpty();
        assertThat(model.<Number> get("$.data[0].retainedPartitionCount")).isEqualTo(10);
        assertThat(model.<Number> get("$.data[0].retention")).isEqualTo(300);
    }

    private LifeCycleCategory mockLifeCycleCategory(LifeCycleCategoryKind kind, int partCount) {
        LifeCycleCategory category = mock(LifeCycleCategory.class);
        when(category.getKind()).thenReturn(kind);
        when(category.getName()).thenReturn(kind.name().toLowerCase());
        when(category.getDisplayName()).thenReturn(kind.name());
        when(category.getRetainedPartitionCount()).thenReturn(partCount);
        when(category.getRetention()).thenReturn(Period.ofDays(partCount*30));
        when(category.getPartitionSize()).thenReturn(Period.ofDays(30));
        when(category.getVersion()).thenReturn(OK_VERSION);
        when(lifeCycleService.findAndLockCategoryByKeyAndVersion(kind, OK_VERSION)).thenReturn(Optional.of(category));
        when(lifeCycleService.findAndLockCategoryByKeyAndVersion(kind, BAD_VERSION)).thenReturn(Optional.empty());
        return category;
    }

    @Test
    public void testUpdateSingleLifeCycleCategory(){
        LifeCycleCategory category = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 10);

        when(lifeCycleService.getCategories()).thenReturn(Collections.singletonList(category));

        LifeCycleCategoryInfo info = new LifeCycleCategoryInfo();
        info.kind = "REGISTER";
        info.retention = 60;
        info.version = OK_VERSION;

        Response response = target("/data/lifecycle/categories/REGISTER").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(category, times(1)).setRetentionDays(60);
    }

    @Test
    public void testUpdateBatchLifeCycleCategories(){
        LifeCycleCategory category1 = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 10);
        LifeCycleCategory category2 = mockLifeCycleCategory(LifeCycleCategoryKind.JOURNAL, 10);
        when(lifeCycleService.getCategories()).thenReturn(Arrays.asList(category1, category2));


        ListInfo<LifeCycleCategoryInfo> infos = new ListInfo<>();
        LifeCycleCategoryInfo info = new LifeCycleCategoryInfo();
        info.kind = "REGISTER";
        info.retention = 60;
        info.version = OK_VERSION;
        infos.data.add(info);

        info = new LifeCycleCategoryInfo();
        info.kind = "JOURNAL";
        info.retention = 90;
        info.version = OK_VERSION;
        infos.data.add(info);

        Response response = target("/data/lifecycle/categories").request().put(Entity.json(infos));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(category1, times(1)).setRetentionDays(60);
        verify(category2, times(1)).setRetentionDays(90);
    }

    @Test
    public void testDataPurgeHistoryRecord(){
        TaskOccurrence occurrence = mockTaskOccurrence();

        when(taskService.getOccurrence(anyLong())).thenReturn(Optional.of(occurrence));
        String response = target("/data/history/1").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number> get("$.id")).isEqualTo(1);
        assertThat(model.<Number> get("$.startDate")).isEqualTo(JUN_2014.toEpochMilli());
        assertThat(model.<Number> get("$.duration")).isEqualTo(10*60*1000);
        assertThat(model.<String>get("$.status")).isEqualTo(TaskStatus.FAILED.toString());
    }

    private TaskOccurrence mockTaskOccurrence() {
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(occurrence.getStartDate()).thenReturn(Optional.of(JUN_2014));
        when(occurrence.getEndDate()).thenReturn(Optional.of(JUN_2014.plus(10, ChronoUnit.MINUTES)));
        when(occurrence.getStatus()).thenReturn(TaskStatus.FAILED);
        when(occurrence.getId()).thenReturn(1L);
        return occurrence;
    }

    @Test
    public void testPurgeHistoryNotStartedYet(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(occurrence.getStartDate()).thenReturn(Optional.empty());
        when(occurrence.getEndDate()).thenReturn(Optional.empty());
        when(occurrence.getStatus()).thenReturn(TaskStatus.NOT_EXECUTED_YET);
        when(occurrence.getId()).thenReturn(1L);

        PurgeHistoryInfo info = new PurgeHistoryInfo(occurrence, Clock.systemDefaultZone());
        assertThat(info.startDate).isNull();
        assertThat(info.duration).isNull();
    }

    @Test
    public void testPurgeHistoryNotFinishedYet(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(occurrence.getStartDate()).thenReturn(Optional.of(JUN_2014));
        when(occurrence.getEndDate()).thenReturn(Optional.empty());
        when(occurrence.getStatus()).thenReturn(TaskStatus.BUSY);
        when(occurrence.getId()).thenReturn(1L);

        PurgeHistoryInfo info = new PurgeHistoryInfo(occurrence, Clock.systemDefaultZone());
        assertThat(info.startDate).isEqualTo(JUN_2014.toEpochMilli());
        assertThat(info.duration).isNotNull();
    }

    @Test
    public void testPurgeHistoryFinishedTask(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(occurrence.getStartDate()).thenReturn(Optional.of(JUN_2014));
        when(occurrence.getEndDate()).thenReturn(Optional.of(JUN_2014.plus(5, ChronoUnit.MINUTES)));
        when(occurrence.getStatus()).thenReturn(TaskStatus.SUCCESS);
        when(occurrence.getId()).thenReturn(1L);

        PurgeHistoryInfo info = new PurgeHistoryInfo(occurrence, Clock.systemDefaultZone());
        assertThat(info.startDate).isEqualTo(JUN_2014.toEpochMilli());
        assertThat(info.duration).isEqualTo(5 * 60 * 1000);
    }

    @Test
    public void testGetLifeCycleCategoriesAsOf(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(taskService.getOccurrence(anyLong())).thenReturn(Optional.of(occurrence));
        when(occurrence.getTriggerTime()).thenReturn(JUN_2014);

        LifeCycleCategory category1 = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 20);
        LifeCycleCategory category2 = mockLifeCycleCategory(LifeCycleCategoryKind.JOURNAL, 10);
        when(lifeCycleService.getCategoriesAsOf(JUN_2014)).thenReturn(Arrays.asList(category1, category2));

        String response = target("/data/history/458/categories").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.data").size()).isEqualTo(2);
        assertThat(model.<String> get("$.data[0].kind")).isEqualTo("REGISTER");
        assertThat(model.<Number> get("$.data[0].retention")).isEqualTo(600);
    }

    private LogEntry mockLogEntry(){
        LogEntry entry = mock(LogEntry.class);
        when(entry.getLogLevel()).thenReturn(Level.INFO);
        when(entry.getMessage()).thenReturn("Some log message");
        when(entry.getTimestamp()).thenReturn(JUN_2014);
        return entry;
    }

    @Test
    public void testGetPurgeLogForOccurenceModel(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(taskService.getOccurrence(anyLong())).thenReturn(Optional.of(occurrence));
        LogEntryFinder finder = mock(LogEntryFinder.class);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(occurrence.getLogsFinder()).thenReturn(finder);
        LogEntry entry = mockLogEntry();
        doReturn(Collections.singletonList(entry)).when(finder).find();

        String response = target("/data/history/1/logs").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number> get("$.total")).isEqualTo(1);
        assertThat(model.<List> get("$.data").size()).isEqualTo(1);
        assertThat(model.<Number> get("$.data[0].timestamp")).isEqualTo(JUN_2014.toEpochMilli());
        assertThat(model.<String> get("$.data[0].logLevel")).isEqualTo(Level.INFO.getName());
        assertThat(model.<String> get("$.data[0].message")).isNotEmpty();
    }

    @Test
    public void testGetPurgeLogForOccurencePaging(){
        TaskOccurrence occurrence = mock(TaskOccurrence.class);
        when(taskService.getOccurrence(anyLong())).thenReturn(Optional.of(occurrence));
        LogEntryFinder finder = mock(LogEntryFinder.class);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(occurrence.getLogsFinder()).thenReturn(finder);
        List<LogEntry> logEntries = Arrays.asList(
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry(),
                mockLogEntry());
        doReturn(logEntries).when(finder).find();

        String response = target("/data/history/1/logs").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(11);
        assertThat(model.<List>get("$.data").size()).isEqualTo(10);
    }

    @Test
    public void testGetPurgeHistoryPaging(){
        List<TaskOccurrence> occurrences = Arrays.asList(mockTaskOccurrence(), mockTaskOccurrence(), mockTaskOccurrence());
        when(taskService.getOccurrences(any(), any())).thenReturn(occurrences);

        String response = target("/data/history").queryParam("start", 0).queryParam("limit", 2).request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<List> get("$.data").size()).isEqualTo(2);
        assertThat(model.<Number> get("$.total")).isEqualTo(3);
    }

    @Test
    public void testGetPurgeHistoryPagingLastPage(){
        List<TaskOccurrence> occurrences = Arrays.asList(mockTaskOccurrence(), mockTaskOccurrence());
        when(taskService.getOccurrences(any(), any())).thenReturn(occurrences);

        String response = target("/data/history").queryParam("start", 0).queryParam("limit", 2).request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<List> get("$.data").size()).isEqualTo(2);
        assertThat(model.<Number> get("$.total")).isEqualTo(2);
    }

    @Test
    public void testGetPurgeHistoryPagingOutOfBound(){
        List<TaskOccurrence> occurrences = Arrays.asList(mockTaskOccurrence(), mockTaskOccurrence());
        when(taskService.getOccurrences(any(), any())).thenReturn(occurrences);

        String response = target("/data/history").queryParam("start", 2).queryParam("limit", 2).request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<List> get("$.data").size()).isEqualTo(0);
        assertThat(model.<Number> get("$.total")).isEqualTo(2);
    }

    @Test
    public void testUpdateSingleLifeCycleCategoryBadVersion(){
        LifeCycleCategory category = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 10);

        when(lifeCycleService.getCategories()).thenReturn(Collections.singletonList(category));

        LifeCycleCategoryInfo info = new LifeCycleCategoryInfo();
        info.kind = "REGISTER";
        info.retention = 60;
        info.version = BAD_VERSION;

        Response response = target("/data/lifecycle/categories/REGISTER").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(category, never()).setRetentionDays(60);
    }

    @Test
    public void testUpdateBatchLifeCycleCategoriesBadVersion(){
        LifeCycleCategory category1 = mockLifeCycleCategory(LifeCycleCategoryKind.REGISTER, 10);
        LifeCycleCategory category2 = mockLifeCycleCategory(LifeCycleCategoryKind.JOURNAL, 10);
        when(lifeCycleService.getCategories()).thenReturn(Arrays.asList(category1, category2));


        ListInfo<LifeCycleCategoryInfo> infos = new ListInfo<>();
        LifeCycleCategoryInfo info = new LifeCycleCategoryInfo();
        info.kind = "REGISTER";
        info.retention = 60;
        info.version = BAD_VERSION;
        infos.data.add(info);

        info = new LifeCycleCategoryInfo();
        info.kind = "JOURNAL";
        info.retention = 90;
        info.version = BAD_VERSION;
        infos.data.add(info);

        Response response = target("/data/lifecycle/categories").request().put(Entity.json(infos));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(category1, never()).setRetentionDays(60);
        verify(category2, never()).setRetentionDays(90);
    }
}
