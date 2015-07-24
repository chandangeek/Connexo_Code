package com.energyict.mdc.dashboard.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DashboardServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (14:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceImplTest {

    private static final long EXPECTED_STATUS_COUNT_VALUE = 97L;

    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private TaskService taskService;
    @Mock
    private SchedulingService schedulingService;

    private DashboardServiceImpl dashboardService;

    @Before
    public void setupService() {
        this.dashboardService = new DashboardServiceImpl(this.taskService, this.connectionTaskService, this.communicationTaskService);
    }

    @Test
    public void testConnectionOverview() {
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.connectionTaskService.getConnectionTaskStatusCount()).thenReturn(statusCounters);

        // Business methods
        TaskStatusOverview overview = this.dashboardService.getConnectionTaskStatusOverview();

        // Asserts
        verify(this.connectionTaskService).getConnectionTaskStatusCount();
        assertThat(overview).isNotNull();
        Set<TaskStatus> missingTaskStatusses = EnumSet.allOf(TaskStatus.class);
        for (Counter<TaskStatus> taskStatusCounter : overview) {
            assertThat(taskStatusCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
            missingTaskStatusses.remove(taskStatusCounter.getCountTarget());
        }
        assertThat(missingTaskStatusses).as("Some TaskStatusses were not reported!").isEmpty();
    }

    @Test
    public void testComSessionSuccessIndicatorOverview() {
        Map<ComSession.SuccessIndicator, Long> counters = new EnumMap<>(ComSession.SuccessIndicator.class);
        for (ComSession.SuccessIndicator successIndicator : ComSession.SuccessIndicator.values()) {
            counters.put(successIndicator, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.connectionTaskService.getConnectionTaskLastComSessionSuccessIndicatorCount()).thenReturn(counters);
        when(this.connectionTaskService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask()).thenReturn(EXPECTED_STATUS_COUNT_VALUE);

        // Business methods
        ComSessionSuccessIndicatorOverview overview = this.dashboardService.getComSessionSuccessIndicatorOverview();

        // Asserts
        assertThat(overview).isNotNull();
        verify(this.connectionTaskService).countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();
        verify(this.connectionTaskService).getConnectionTaskLastComSessionSuccessIndicatorCount();
        assertThat(overview.iterator().hasNext()).isTrue();
        for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : overview) {
            assertThat(successIndicatorCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        }
        assertThat(overview.getTotalCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE * 3);
        assertThat(overview.getAtLeastOneTaskFailedCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
    }

    @Test
    public void testComPortPoolBreakdownWithoutComPortPools() {
        when(this.connectionTaskService.getComPortPoolBreakdown(anySet())).thenReturn(new HashMap<ComPortPool, Map<TaskStatus, Long>>());

        // Business methods
        ComPortPoolBreakdown breakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        verify(this.connectionTaskService).getComPortPoolBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComPortPoolBreakdownWithComPortPoolsButNoConnections() {
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(this.engineConfigurationService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool));
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        Map<ComPortPool, Map<TaskStatus, Long>> mockedBreakdown = new HashMap<>();
        mockedBreakdown.put(comPortPool, statusCounters);
        when(this.connectionTaskService.getComPortPoolBreakdown(anySet())).thenReturn(mockedBreakdown);

        // Business methods
        ComPortPoolBreakdown actualBreakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        verify(this.connectionTaskService).getComPortPoolBreakdown(anySet());
        assertThat(actualBreakdown).isNotNull();
        assertThat(actualBreakdown.iterator().hasNext()).isTrue();
        assertThat(actualBreakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(actualBreakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(actualBreakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(actualBreakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testConnectionTypeBreakdownWithoutConnectionTypes() {
        when(this.connectionTaskService.getConnectionTypeBreakdown(anySet())).thenReturn(new HashMap<>());

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        verify(this.connectionTaskService).getConnectionTypeBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testConnectionTypeBreakdownWithConnectionTypesButNoConnections() {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(connectionTypePluggableClass));
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> mockedBreakdown = new HashMap<>();
        mockedBreakdown.put(connectionTypePluggableClass, statusCounters);
        when(this.connectionTaskService.getConnectionTypeBreakdown(anySet())).thenReturn(mockedBreakdown);

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        verify(this.connectionTaskService).getConnectionTypeBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testConnectionTaskDeviceTypeBreakdownWithoutDeviceTypes() {
        when(this.connectionTaskService.getDeviceTypeBreakdown(anySet())).thenReturn(new HashMap<>());

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getConnectionTasksDeviceTypeBreakdown();

        // Asserts
        verify(this.connectionTaskService).getDeviceTypeBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testConnectionTaskDeviceTypeBreakdownWithDeviceTypesButNoConnections() {
        DeviceType deviceType = mock(DeviceType.class);
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(deviceType));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        Map<DeviceType, Map<TaskStatus, Long>> mockedBreakdown = new HashMap<>();
        mockedBreakdown.put(deviceType, statusCounters);
        when(this.connectionTaskService.getDeviceTypeBreakdown(anySet())).thenReturn(mockedBreakdown);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getConnectionTasksDeviceTypeBreakdown();

        // Asserts
        verify(this.connectionTaskService).getDeviceTypeBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testCommunicationOverview() {
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.communicationTaskService.getComTaskExecutionStatusCount()).thenReturn(statusCounters);

        // Business methods
        TaskStatusOverview overview = this.dashboardService.getCommunicationTaskStatusOverview();

        // Asserts
        verify(this.communicationTaskService).getComTaskExecutionStatusCount();
        assertThat(overview).isNotNull();
        Set<TaskStatus> missingTaskStatusses = EnumSet.allOf(TaskStatus.class);
        for (Counter<TaskStatus> taskStatusCounter : overview) {
            assertThat(taskStatusCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
            missingTaskStatusses.remove(taskStatusCounter.getCountTarget());
        }
        assertThat(missingTaskStatusses).as("Some TaskStatusses were not reported!").isEmpty();
    }

    @Test
    public void testComTaskExecutionsDeviceTypeBreakdownWithoutDeviceTypes() {
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.<DeviceType>emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getCommunicationTasksDeviceTypeBreakdown();

        // Asserts
        verify(this.communicationTaskService, never()).getComTaskExecutionStatusCount(any(ComTaskExecutionFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComTaskExecutionsDeviceTypeBreakdownWithDeviceTypesButNoComTaskExecutions() {
        DeviceType deviceType = mock(DeviceType.class);
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(deviceType));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        Map<DeviceType, Map<TaskStatus, Long>> mockedBreakdown = new HashMap<>();
        mockedBreakdown.put(deviceType, statusCounters);
        when(this.communicationTaskService.getCommunicationTasksDeviceTypeBreakdown(anySet())).thenReturn(mockedBreakdown);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getCommunicationTasksDeviceTypeBreakdown();

        // Asserts
        verify(this.communicationTaskService).getCommunicationTasksDeviceTypeBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testComTaskExecutionsBreakdownWithoutComTasks() {
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.<DeviceType>emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        List<ComTask> comTasks = new ArrayList<>(0);
        Finder<ComTask> comTaskFinder = mockFinder(comTasks);
        when(this.taskService.findAllComTasks()).thenReturn(comTaskFinder);

        // Business methods
        ComTaskBreakdown breakdown = this.dashboardService.getCommunicationTasksBreakdown();

        // Asserts
        verify(this.communicationTaskService, never()).getComTaskExecutionStatusCount(any(ComTaskExecutionFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComTaskExecutionsBreakdownWithDeviceTypesButNoComTaskExecutions() {
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.<DeviceType>emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        List<ComTask> comTasks = new ArrayList<>(1);
        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(97L);
        when(comTask.getName()).thenReturn("testComTaskExecutionsDeviceTypeBreakdownWithDeviceTypesButNoComTaskExecutions");
        comTasks.add(comTask);
        Finder<ComTask> comTaskFinder = mockFinder(comTasks);
        when(this.taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.communicationTaskService.getComTaskExecutionStatusCount(any(ComTaskExecutionFilterSpecification.class))).thenReturn(statusCounters);

        // Business methods
        ComTaskBreakdown breakdown = this.dashboardService.getCommunicationTasksBreakdown();

        // Asserts
        ArgumentCaptor<ComTaskExecutionFilterSpecification> filterCaptor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(this.communicationTaskService).getComTaskExecutionStatusCount(filterCaptor.capture());
        assertThat(filterCaptor.getValue().comTasks).hasSize(1);
        assertThat(filterCaptor.getValue().comTasks.iterator().next()).isEqualTo(comTask);
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testComTaskExecutionsComScheduleBreakdownWithoutComSchedules() {
        List<ComSchedule> comSchedules = new ArrayList<>(0);
        when(this.schedulingService.findAllSchedules()).thenReturn(comSchedules);

        // Business methods
        ComScheduleBreakdown breakdown = this.dashboardService.getCommunicationTasksComScheduleBreakdown();

        // Asserts
        verify(this.communicationTaskService, never()).getComTaskExecutionStatusCount(any(ComTaskExecutionFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComTaskExecutionsComScheduleBreakdownWithComSchedulesButNoComTaskExecutions() {
        ComSchedule comSchedule = mock(ComSchedule.class);
        List<ComSchedule> comSchedules = new ArrayList<>();
        comSchedules.add(comSchedule);
        when(this.schedulingService.findAllSchedules()).thenReturn(comSchedules);
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        Map<ComSchedule, Map<TaskStatus, Long>> mockedBreakDown = new HashMap<>();
        mockedBreakDown.put(comSchedule, statusCounters);
        when(this.communicationTaskService.getCommunicationTasksComScheduleBreakdown(anySet())).thenReturn(mockedBreakDown);

        // Business methods
        ComScheduleBreakdown breakdown = this.dashboardService.getCommunicationTasksComScheduleBreakdown();

        // Asserts
        verify(this.communicationTaskService).getCommunicationTasksComScheduleBreakdown(anySet());
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testComTaskExecutionCompletionCodeOverview() {
        Map<CompletionCode, Long> counters = new EnumMap<>(CompletionCode.class);
        for (CompletionCode completionCode : CompletionCode.values()) {
            counters.put(completionCode, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.communicationTaskService.getComTaskLastComSessionHighestPriorityCompletionCodeCount()).thenReturn(counters);

        // Business methods
        ComCommandCompletionCodeOverview overview = this.dashboardService.getCommunicationTaskCompletionResultOverview();

        // Asserts
        assertThat(overview).isNotNull();
        verify(this.communicationTaskService).getComTaskLastComSessionHighestPriorityCompletionCodeCount();
        assertThat(overview.iterator().hasNext()).isTrue();
        for (Counter<CompletionCode> completionCodeCounter : overview) {
            assertThat(completionCodeCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        }
        assertThat(overview.getTotalCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE * CompletionCode.values().length);
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}