package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private EngineModelService engineModelService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;

    private DashboardServiceImpl dashboardService;

    @Before
    public void setupService () {
        this.dashboardService = new DashboardServiceImpl(this.engineModelService, this.deviceConfigurationService, this.deviceDataService, this.protocolPluggableService);
    }

    @Test
    public void testConnectionOverview () {
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getConnectionTaskStatusCount()).thenReturn(statusCounters);

        // Business methods
        TaskStatusOverview overview = this.dashboardService.getConnectionTaskStatusOverview();

        // Asserts
        verify(this.deviceDataService).getConnectionTaskStatusCount();
        assertThat(overview).isNotNull();
        Set<TaskStatus> missingTaskStatusses = EnumSet.allOf(TaskStatus.class);
        for (Counter<TaskStatus> taskStatusCounter : overview) {
            assertThat(taskStatusCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
            missingTaskStatusses.remove(taskStatusCounter.getCountTarget());
        }
        assertThat(missingTaskStatusses).as("Some TaskStatusses were not reported!").isEmpty();
    }

    @Test
    public void testComSessionSuccessIndicatorOverview () {
        Map<ComSession.SuccessIndicator, Long> counters = new EnumMap<>(ComSession.SuccessIndicator.class);
        for (ComSession.SuccessIndicator successIndicator : ComSession.SuccessIndicator.values()) {
            counters.put(successIndicator, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getConnectionTaskLastComSessionSuccessIndicatorCount()).thenReturn(counters);
        when(this.deviceDataService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask()).thenReturn(EXPECTED_STATUS_COUNT_VALUE);

        // Business methods
        ComSessionSuccessIndicatorOverview overview = this.dashboardService.getComSessionSuccessIndicatorOverview();

        // Asserts
        assertThat(overview).isNotNull();
        verify(this.deviceDataService).countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();
        verify(this.deviceDataService).getConnectionTaskLastComSessionSuccessIndicatorCount();
        assertThat(overview.iterator().hasNext()).isTrue();
        for (Counter<ComSession.SuccessIndicator> successIndicatorCounter : overview) {
            assertThat(successIndicatorCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        }
        assertThat(overview.getTotalCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE * 3);
        assertThat(overview.getAtLeastOneTaskFailedCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
    }

    @Test
    public void testComPortPoolBreakdownWithoutComPortPools () {
        when(this.engineModelService.findAllComPortPools()).thenReturn(Collections.<ComPortPool>emptyList());

        // Business methods
        ComPortPoolBreakdown breakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        verify(this.deviceDataService, never()).getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComPortPoolBreakdownWithComPortPoolsButNoConnections () {
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(this.engineModelService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool));
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class))).thenReturn(statusCounters);

        // Business methods
        ComPortPoolBreakdown breakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        ArgumentCaptor<ConnectionTaskFilterSpecification> filterCaptor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(this.deviceDataService).getConnectionTaskStatusCount(filterCaptor.capture());
        assertThat(filterCaptor.getValue().comPortPools).hasSize(1);
        assertThat(filterCaptor.getValue().comPortPools.iterator().next()).isEqualTo(comPortPool);
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testConnectionTypeBreakdownWithoutConnectionTypes () {
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Collections.<ConnectionTypePluggableClass>emptyList());

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        verify(this.deviceDataService, never()).getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testConnectionTypeBreakdownWithConnectionTypesButNoConnections () {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(connectionTypePluggableClass));
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class))).thenReturn(statusCounters);

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        verify(this.deviceDataService).getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testDeviceTypeBreakdownWithoutDeviceTypes () {
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.<DeviceType>emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getDeviceTypeBreakdown();

        // Asserts
        verify(this.deviceDataService, never()).getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class));
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testDeviceTypeBreakdownWithDeviceTypesButNoConnections () {
        DeviceType deviceType = mock(DeviceType.class);
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(deviceType));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getConnectionTaskStatusCount(any(ConnectionTaskFilterSpecification.class))).thenReturn(statusCounters);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getDeviceTypeBreakdown();

        // Asserts
        ArgumentCaptor<ConnectionTaskFilterSpecification> filterCaptor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(this.deviceDataService).getConnectionTaskStatusCount(filterCaptor.capture());
        assertThat(filterCaptor.getValue().deviceTypes).hasSize(1);
        assertThat(filterCaptor.getValue().deviceTypes.iterator().next()).isEqualTo(deviceType);
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isEqualTo(6 * EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalSuccessCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
        assertThat(breakdown.getTotalFailedCount()).isEqualTo(2 * EXPECTED_STATUS_COUNT_VALUE); // Status Failed + Never Completed
        assertThat(breakdown.getTotalPendingCount()).isEqualTo(3 * EXPECTED_STATUS_COUNT_VALUE);// Status Pending + Busy + Retrying
    }

    @Test
    public void testCommunicationOverview () {
        Map<TaskStatus, Long> statusCounters = new EnumMap<>(TaskStatus.class);
        for (TaskStatus taskStatus : TaskStatus.values()) {
            statusCounters.put(taskStatus, EXPECTED_STATUS_COUNT_VALUE);
        }
        when(this.deviceDataService.getComTaskExecutionStatusCount()).thenReturn(statusCounters);

        // Business methods
        TaskStatusOverview overview = this.dashboardService.getCommunicationTaskStatusOverview();

        // Asserts
        verify(this.deviceDataService).getComTaskExecutionStatusCount();
        assertThat(overview).isNotNull();
        Set<TaskStatus> missingTaskStatusses = EnumSet.allOf(TaskStatus.class);
        for (Counter<TaskStatus> taskStatusCounter : overview) {
            assertThat(taskStatusCounter.getCount()).isEqualTo(EXPECTED_STATUS_COUNT_VALUE);
            missingTaskStatusses.remove(taskStatusCounter.getCountTarget());
        }
        assertThat(missingTaskStatusses).as("Some TaskStatusses were not reported!").isEmpty();
    }

}