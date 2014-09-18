package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.impl.TaskStatusBreakdownCounterImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/18/14.
 */
public class CommunicationOverviewResourceTest extends DashboardRESTJerseyTest {

    @Test
    public void testGetCommunicationOverview() throws Exception {
        TaskStatusOverview statusOverview = createConnectionStatusOverview();
        when(dashboardService.getCommunicationTaskStatusOverview()).thenReturn(statusOverview);
        ComCommandCompletionCodeOverview comCommandCompletionCodeOverview = createComCommandCompletionCodeOverview();
        when(dashboardService.getCommunicationTaskCompletionResultOverview()).thenReturn(comCommandCompletionCodeOverview);
        ComScheduleBreakdown comScheduleBreakdown = createComScheduleBreakdown();
        when(dashboardService.getCommunicationTasksComScheduleBreakdown()).thenReturn(comScheduleBreakdown);
        ComTaskBreakdown comTaskBreakdown = createComTaskBreakdown();
        when(dashboardService.getCommunicationTasksBreakdown()).thenReturn(comTaskBreakdown);
        DeviceTypeBreakdown deviceTypeBreakdown = createDeviceTypeBreakdown();
        when(dashboardService.getCommunicationTasksDeviceTypeBreakdown()).thenReturn(deviceTypeBreakdown);

        String response = target("/communicationoverview").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[0].counters")).isEmpty();
        assertThat(jsonModel.<Integer>get("$.communicationSummary.total")).isEqualTo(169);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Success')].count[0]")).isEqualTo(15);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Pending')].count[0]")).isEqualTo(98);
        assertThat(jsonModel.<Integer>get("$.communicationSummary.counters[?(@.displayName=='Failed')].count[0]")).isEqualTo(56);

        assertThat(jsonModel.<List>get("$.communicationSummary.counters[0].id")).containsExactly("Waiting");
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[1].id")).contains("Busy", "Retrying", "Pending").hasSize(3);
        assertThat(jsonModel.<List>get("$.communicationSummary.counters[2].id")).contains("NeverCompleted", "Failed").hasSize(2);

        assertThat(jsonModel.<List<Integer>>get("$.overviews[*].counters[*].count")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));
        assertThat(jsonModel.<List<Integer>>get("$.breakdowns[*].counters[*].failingCount")).isSortedAccordingTo((c1,c2)->c2.compareTo(c1));
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

    private TaskStatusOverview createConnectionStatusOverview() {
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
        when(overview.iterator()).thenReturn(counters.iterator());
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
