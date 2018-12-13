/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.CommunicationTaskOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.impl.TaskStatusBreakdownCounterImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.jayway.jsonpath.JsonModel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/18/14.
 */
public class CommunicationOverviewResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testGetCommunicationOverviewWithoutDeviceGroup() throws Exception {
        TaskStatusOverview statusOverview = createCommunicationStatusOverview();
        ComCommandCompletionCodeOverview comCommandCompletionCodeOverview = createComCommandCompletionCodeOverview();
        ComScheduleBreakdown comScheduleBreakdown = createComScheduleBreakdown();
        ComTaskBreakdown comTaskBreakdown = createComTaskBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = createDeviceTypeBreakdown();
        CommunicationTaskOverview overview = mock(CommunicationTaskOverview.class);
        when(overview.getStatusOverview()).thenReturn(statusOverview);
        when(overview.getCommunicationTaskCompletionResultOverview()).thenReturn(comCommandCompletionCodeOverview);
        when(overview.getComScheduleBreakdown()).thenReturn(comScheduleBreakdown);
        when(overview.getComTaskBreakdown()).thenReturn(comTaskBreakdown);
        when(overview.getDeviceTypeBreakdown()).thenReturn(deviceTypeBreakdown);
        when(dashboardService.getCommunicationTaskOverview()).thenReturn(overview);

        String response = target("/communicationoverview").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.total")).isEqualTo(169);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.target")).isNull();
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Successful')].count[0]")).isEqualTo(15);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Ongoing')].count[0]")).isEqualTo(98);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Failed')].count[0]")).isEqualTo(56);

        assertThat(jsonModel.<List>get("$.communicationSummary.counters[0].id")).containsExactly("Waiting");
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[1].id")).contains("Busy", "Retrying", "Pending").hasSize(3);
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[2].id")).contains("NeverCompleted", "Failed").hasSize(2);

        assertThat(jsonModel.<List<Integer>>get("$.overviews[*].counters[*].count")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));
        assertThat(jsonModel.<List<Integer>>get("$.breakdowns[*].counters[*].failingCount")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));
    }

    @Test
    public void testGetCommunicationOverviewWithDeviceGroup() throws Exception {
        int deviceGroupId = 321;

        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn((long) deviceGroupId);
        when(endDeviceGroup.getName()).thenReturn("Northern region");
        when(meteringGroupsService.findEndDeviceGroup(deviceGroupId)).thenReturn(Optional.of(endDeviceGroup));

        TaskStatusOverview statusOverview = createCommunicationStatusOverview();
        ComCommandCompletionCodeOverview comCommandCompletionCodeOverview = createComCommandCompletionCodeOverview();
        ComScheduleBreakdown comScheduleBreakdown = createComScheduleBreakdown();
        ComTaskBreakdown comTaskBreakdown = createComTaskBreakdown();
        DeviceTypeBreakdown deviceTypeBreakdown = createDeviceTypeBreakdown();
        CommunicationTaskOverview overview = mock(CommunicationTaskOverview.class);
        when(overview.getStatusOverview()).thenReturn(statusOverview);
        when(overview.getCommunicationTaskCompletionResultOverview()).thenReturn(comCommandCompletionCodeOverview);
        when(overview.getComScheduleBreakdown()).thenReturn(comScheduleBreakdown);
        when(overview.getComTaskBreakdown()).thenReturn(comTaskBreakdown);
        when(overview.getDeviceTypeBreakdown()).thenReturn(deviceTypeBreakdown);
        when(dashboardService.getCommunicationTaskOverview(endDeviceGroup)).thenReturn(overview);

        DataCollectionKpi dataCollectionKpi = mockDataCommunicationKpi();
        when(dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup)).thenReturn(Optional.of(dataCollectionKpi));

        String response = target("/communicationoverview").queryParam("filter", ExtjsFilter.filter("deviceGroup", (long) deviceGroupId)).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.total")).isEqualTo(169);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.target")).isEqualTo(100);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Successful')].count[0]")).isEqualTo(15);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Ongoing')].count[0]")).isEqualTo(98);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Failed')].count[0]")).isEqualTo(56);

        assertThat(jsonModel.<List>get("$.communicationSummary.counters[0].id")).containsExactly("Waiting");
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[1].id")).contains("Busy", "Retrying", "Pending").hasSize(3);
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[2].id")).contains("NeverCompleted", "Failed").hasSize(2);
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[*].name")).containsExactly("Success", "Ongoing", "Failed");

        assertThat(jsonModel.<List<Integer>>get("$.overviews[*].counters[*].count")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));
        assertThat(jsonModel.<List<Integer>>get("$.breakdowns[*].counters[*].failingCount")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));

        assertThat(jsonModel.<Integer>get("$.deviceGroup.id")).isEqualTo(321);
        assertThat(jsonModel.<String>get("$.deviceGroup.name")).isEqualTo("Northern region");
        assertThat(jsonModel.<String>get("$.deviceGroup.alias")).isEqualTo("deviceGroups");
        assertThat(jsonModel.<Object>get("$.kpi")).isNotNull();
        assertThat(jsonModel.<String>get("$.kpi.series[0].name")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.kpi.series[1].name")).isEqualTo("Ongoing");
        assertThat(jsonModel.<String>get(("$.kpi.series[2].name"))).isEqualTo("Failed");
        assertThat(jsonModel.<String>get(("$.kpi.series[3].name"))).isEqualTo("Target");
        assertThat(jsonModel.<Integer>get(("$.kpi.series[0].data[0]"))).isEqualTo(0);
        assertThat(jsonModel.<Integer>get(("$.kpi.series[1].data[0]"))).isEqualTo(99);
        assertThat(jsonModel.<Integer>get(("$.kpi.series[2].data[0]"))).isEqualTo(1);
        assertThat(jsonModel.<Integer>get(("$.kpi.series[3].data[0]"))).isEqualTo(100);
    }

    private DataCollectionKpi mockDataCommunicationKpi() {
        DataCollectionKpi dataCollectionKpi = mock(DataCollectionKpi.class);
        when(dataCollectionKpi.calculatesComTaskExecutionKpi()).thenReturn(true);
        when(dataCollectionKpi.comTaskExecutionKpiCalculationIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        when(dataCollectionKpi.getDisplayRange()).thenReturn(TimeDuration.days(1));
        List<DataCollectionKpiScore> kpiScores = new ArrayList<>();
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant(), 0, 99, 1, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 14, 0, 0).atZone(ZoneId.systemDefault()).toInstant(), 10, 80, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 14, 15, 0).atZone(ZoneId.systemDefault()).toInstant(), 20, 70, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 14, 30, 0).atZone(ZoneId.systemDefault()).toInstant(), 30, 60, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 14, 45, 0).atZone(ZoneId.systemDefault()).toInstant(), 40, 50, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 15, 0, 0).atZone(ZoneId.systemDefault()).toInstant(), 50, 40, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 15, 15, 0).atZone(ZoneId.systemDefault()).toInstant(), 60, 30, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 15, 30, 0).atZone(ZoneId.systemDefault()).toInstant(), 70, 20, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 15, 45, 0).atZone(ZoneId.systemDefault()).toInstant(), 80, 10, 10, 100));
        kpiScores.add(mockDataCommunicationKpiScore(LocalDateTime.of(2014, 10, 1, 16, 0, 0).atZone(ZoneId.systemDefault()).toInstant(), 90, 0, 10, 100));
        when(dataCollectionKpi.getComTaskExecutionKpiScores(anyObject())).thenReturn(kpiScores);
        return dataCollectionKpi;
    }

    private DataCollectionKpiScore mockDataCommunicationKpiScore(Instant timeStamp, long success, long ongoing, long failed, long target) {
        DataCollectionKpiScore mock = mock(DataCollectionKpiScore.class);
        when(mock.getTimestamp()).thenReturn(timeStamp);
        when(mock.getSuccess()).thenReturn(BigDecimal.valueOf(success));
        when(mock.getOngoing()).thenReturn(BigDecimal.valueOf(ongoing));
        when(mock.getFailed()).thenReturn(BigDecimal.valueOf(failed));
        when(mock.getTarget()).thenReturn(BigDecimal.valueOf(target));
        return mock;
    }


    private DeviceTypeBreakdown createDeviceTypeBreakdown() {
        DeviceTypeBreakdown comTaskBreakdown = mock(DeviceTypeBreakdown.class);
        when(comTaskBreakdown.getTotalCount()).thenReturn((long) (234 + 4 + 411));
        when(comTaskBreakdown.getTotalFailedCount()).thenReturn(234L);
        when(comTaskBreakdown.getTotalPendingCount()).thenReturn(4L);
        when(comTaskBreakdown.getTotalSuccessCount()).thenReturn(411L);
        List<TaskStatusBreakdownCounter<DeviceType>> counters = new ArrayList<>();
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockDeviceType(1, "Device type 1"), 111L, 125L, 111L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockDeviceType(2, "Device Type 2"), 22L, 23L, 178L));
        when(comTaskBreakdown.iterator()).thenReturn(counters.iterator());

        return comTaskBreakdown;
    }

    private ComTaskBreakdown createComTaskBreakdown() {
        ComTaskBreakdown comTaskBreakdown = mock(ComTaskBreakdown.class);
        when(comTaskBreakdown.getTotalCount()).thenReturn((long) (234 + 4 + 411));
        when(comTaskBreakdown.getTotalFailedCount()).thenReturn(234L);
        when(comTaskBreakdown.getTotalPendingCount()).thenReturn(4L);
        when(comTaskBreakdown.getTotalSuccessCount()).thenReturn(411L);
        List<TaskStatusBreakdownCounter<ComTask>> counters = new ArrayList<>();
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockTask(1, "Task 1"), 111L, 125L, 111L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockTask(2, "Schedule 2"), 22L, 23L, 178L));
        when(comTaskBreakdown.iterator()).thenReturn(counters.iterator());

        return comTaskBreakdown;
    }

    private ComCommandCompletionCodeOverview createComCommandCompletionCodeOverview() {
        ComCommandCompletionCodeOverview mock = mock(ComCommandCompletionCodeOverview.class);
        when(mock.getTotalCount()).thenReturn(100L);
        List<Counter<CompletionCode>> counters = new ArrayList<>();
        counters.add(createCounter(CompletionCode.IOError, 1L));
        counters.add(createCounter(CompletionCode.ConfigurationWarning, 2L));
        counters.add(createCounter(CompletionCode.ConfigurationError, 3L));
        counters.add(createCounter(CompletionCode.TimeError, 4L));
        counters.add(createCounter(CompletionCode.ProtocolError, 5L));
        when(mock.iterator()).thenReturn(counters.iterator());
        return mock;
    }

    private TaskStatusOverview createCommunicationStatusOverview() {
        TaskStatusOverview overview = mock(TaskStatusOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<TaskStatus>> counters = new ArrayList<>();
        counters.add(createCounter(TaskStatus.Busy, 41L));
        counters.add(createCounter(TaskStatus.OnHold, 27L));
        counters.add(createCounter(TaskStatus.Retrying, 15L));
        counters.add(createCounter(TaskStatus.NeverCompleted, 15L));
        counters.add(createCounter(TaskStatus.Waiting, 15L));
        counters.add(createCounter(TaskStatus.Pending, 42L));
        counters.add(createCounter(TaskStatus.Failed, 41L));
        when(overview.iterator()).thenAnswer(invocationMock->counters.iterator());
        return overview;
    }

    private <C> Counter<C> createCounter(C status, Long count) {
        Counter<C> counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(count);
        when(counter.getCountTarget()).thenReturn(status);
        return counter;
    }

    private ComScheduleBreakdown createComScheduleBreakdown() {
        ComScheduleBreakdown mock = mock(ComScheduleBreakdown.class);
        when(mock.getTotalCount()).thenReturn((long) (234+4+411));
        when(mock.getTotalFailedCount()).thenReturn(234L);
        when(mock.getTotalPendingCount()).thenReturn(4L);
        when(mock.getTotalSuccessCount()).thenReturn(411L);
        List<TaskStatusBreakdownCounter<ComSchedule>> counters = new ArrayList<>();
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComSchedule(1, "Schedule 1"), 11L, 25L, 11L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComSchedule(2, "Schedule 2"), 411L, 233L, 78L));
        when(mock.iterator()).thenReturn(counters.iterator());
        return mock;
    }

    private ComSchedule mockComSchedule(long id, String name) {
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getId()).thenReturn(id);
        when(comSchedule.getName()).thenReturn(name);
        return comSchedule;
    }

    private ComTask mockTask(long id, String name) {
        ComTask comSchedule = mock(ComTask.class);
        when(comSchedule.getId()).thenReturn(id);
        when(comSchedule.getName()).thenReturn(name);
        return comSchedule;
    }

    private DeviceType mockDeviceType(long id, String name) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(id);
        when(deviceType.getName()).thenReturn(name);
        return deviceType;
    }

}
