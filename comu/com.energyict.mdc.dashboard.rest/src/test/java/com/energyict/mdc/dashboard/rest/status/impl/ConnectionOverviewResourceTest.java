package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.impl.TaskStatusBreakdownCounterImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ConnectionOverviewResourceTest extends DashboardApplicationJerseyTest {

    private ComSessionSuccessIndicatorOverview createComTaskCompletionOverview() {
        ComSessionSuccessIndicatorOverview overview = mock(ComSessionSuccessIndicatorOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<ComSession.SuccessIndicator>> counters = new ArrayList<>();
        counters.add(createCounter(ComSession.SuccessIndicator.Success, 101L));
        counters.add(createCounter(ComSession.SuccessIndicator.Broken, 12L));
        counters.add(createCounter(ComSession.SuccessIndicator.SetupError, 41L));
        when(overview.iterator()).thenReturn(counters.iterator());
        return overview;
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

    @Test
    public void testGetOverviewWithoutDeviceGroup() throws UnsupportedEncodingException {
        TaskStatusOverview taskStatusOverview = createConnectionStatusOverview();
        when(dashboardService.getConnectionTaskStatusOverview()).thenReturn(taskStatusOverview);
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = createComTaskCompletionOverview();
        when(dashboardService.getComSessionSuccessIndicatorOverview()).thenReturn(comSessionSuccessIndicatorOverview);
        ComPortPoolBreakdown comPortPoolBreakdown = createComPortPoolBreakdown();
        when(dashboardService.getComPortPoolBreakdown()).thenReturn(comPortPoolBreakdown);
        ConnectionTypeBreakdown connectionStatusBreakdown = createConnectionTypeBreakdown();
        when(dashboardService.getConnectionTypeBreakdown()).thenReturn(connectionStatusBreakdown);
        DeviceTypeBreakdown deviceTypeBreakdown=createDeviceTypeBreakdown();
        when(dashboardService.getConnectionTasksDeviceTypeBreakdown()).thenReturn(deviceTypeBreakdown);

        ConnectionOverviewInfo connectionOverviewInfo = target("/connectionoverview").request().get(ConnectionOverviewInfo.class);

        Comparator<TaskCounterInfo> counterInfoComparator = (o1, o2) -> Long.valueOf(o2.count).compareTo(o1.count);
        assertThat(connectionOverviewInfo.connectionSummary.counters).hasSize(3);
        assertThat(connectionOverviewInfo.overviews.get(0).counters).isSortedAccordingTo(counterInfoComparator);
        assertThat(connectionOverviewInfo.overviews.get(1).counters).isSortedAccordingTo(counterInfoComparator);

        Comparator<TaskBreakdownInfo> taskBreakdownInfoComparator = (o1, o2) -> Long.valueOf(o2.failedCount).compareTo(o1.failedCount);
        assertThat(connectionOverviewInfo.breakdowns.get(0).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(1).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(2).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.kpi).isNull();
        assertThat(connectionOverviewInfo.deviceGroup).isNull();
    }

    @Test
    public void testGetOverviewWithDeviceGroup() throws UnsupportedEncodingException {
        int deviceGroupId = 123;

        TaskStatusOverview taskStatusOverview = createConnectionStatusOverview();
        QueryEndDeviceGroup endDeviceGroup = mock(QueryEndDeviceGroup.class);
        when(dashboardService.getConnectionTaskStatusOverview(endDeviceGroup)).thenReturn(taskStatusOverview);
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = createComTaskCompletionOverview();
        when(dashboardService.getComSessionSuccessIndicatorOverview(endDeviceGroup)).thenReturn(comSessionSuccessIndicatorOverview);
        ComPortPoolBreakdown comPortPoolBreakdown = createComPortPoolBreakdown();
        when(dashboardService.getComPortPoolBreakdown(endDeviceGroup)).thenReturn(comPortPoolBreakdown);
        ConnectionTypeBreakdown connectionStatusBreakdown = createConnectionTypeBreakdown();
        when(dashboardService.getConnectionTypeBreakdown(endDeviceGroup)).thenReturn(connectionStatusBreakdown);
        DeviceTypeBreakdown deviceTypeBreakdown=createDeviceTypeBreakdown();
        when(dashboardService.getConnectionTasksDeviceTypeBreakdown(endDeviceGroup)).thenReturn(deviceTypeBreakdown);
        DataCollectionKpi dataCollectionKpi = mockDataCollectionKpi();
        when(dataCollectionKpiService.findDataCollectionKpi(endDeviceGroup)).thenReturn(Optional.of(dataCollectionKpi));
        when(meteringGroupsService.findQueryEndDeviceGroup(deviceGroupId)).thenReturn(com.google.common.base.Optional.of(endDeviceGroup));
        when(endDeviceGroup.getId()).thenReturn((long) deviceGroupId);
        when(endDeviceGroup.getName()).thenReturn("South region");

        ConnectionOverviewInfo connectionOverviewInfo = target("/connectionoverview").queryParam("filter", ExtjsFilter.filter("deviceGroup", (long) deviceGroupId)).request().get(ConnectionOverviewInfo.class);

        Comparator<TaskCounterInfo> counterInfoComparator = (o1, o2) -> Long.valueOf(o2.count).compareTo(o1.count);
        assertThat(connectionOverviewInfo.connectionSummary.counters).hasSize(3);
        assertThat(connectionOverviewInfo.overviews.get(0).counters).isSortedAccordingTo(counterInfoComparator);
        assertThat(connectionOverviewInfo.overviews.get(1).counters).isSortedAccordingTo(counterInfoComparator);

        Comparator<TaskBreakdownInfo> taskBreakdownInfoComparator = (o1, o2) -> Long.valueOf(o2.failedCount).compareTo(o1.failedCount);
        assertThat(connectionOverviewInfo.breakdowns.get(0).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(1).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(2).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.kpi).isNotNull();
        assertThat(connectionOverviewInfo.kpi.get(0).name).isEqualTo("Success");
        assertThat(connectionOverviewInfo.kpi.get(1).name).isEqualTo("Ongoing");
        assertThat(connectionOverviewInfo.kpi.get(2).name).isEqualTo("Failed");
        assertThat(connectionOverviewInfo.kpi.get(3).name).isEqualTo("Target");
        assertThat(connectionOverviewInfo.deviceGroup.id).isEqualTo(123);
        assertThat(connectionOverviewInfo.deviceGroup.name).isEqualTo("South region");
        assertThat(connectionOverviewInfo.deviceGroup.alias).isEqualTo("deviceGroups");
    }

    private DataCollectionKpi mockDataCollectionKpi() {
        DataCollectionKpi dataCollectionKpi = mock(DataCollectionKpi.class);
        when(dataCollectionKpi.calculatesConnectionSetupKpi()).thenReturn(true);
        when(dataCollectionKpi.connectionSetupKpiCalculationIntervalLength()).thenReturn(Optional.of(Duration.ofMinutes(15)));
        List<DataCollectionKpiScore> kpiScores = new ArrayList<>();
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,14, 0, 0).toInstant(ZoneOffset.UTC)), 10, 80, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,14,15, 0).toInstant(ZoneOffset.UTC)), 20, 70, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,14,30, 0).toInstant(ZoneOffset.UTC)), 30, 60, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,14,45, 0).toInstant(ZoneOffset.UTC)), 40, 50, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,15, 0, 0).toInstant(ZoneOffset.UTC)), 50, 40, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,15,15, 0).toInstant(ZoneOffset.UTC)), 60, 30, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,15,30, 0).toInstant(ZoneOffset.UTC)), 70, 20, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014,10,1,15,45, 0).toInstant(ZoneOffset.UTC)), 80, 10, 10, 100));
        kpiScores.add(mockDataCollectionKpiScore(Date.from(LocalDateTime.of(2014, 10, 1, 16, 0, 0).toInstant(ZoneOffset.UTC)), 90,  0, 10, 100));
        when(dataCollectionKpi.getConnectionSetupKpiScores(anyObject())).thenReturn(kpiScores);
        return dataCollectionKpi;
    }

    private DataCollectionKpiScore mockDataCollectionKpiScore(Date timeStamp, long success, long ongoing, long failed, long target) {
        DataCollectionKpiScore mock = mock(DataCollectionKpiScore.class);
        when(mock.getTimestamp()).thenReturn(timeStamp);
        when(mock.getSuccess()).thenReturn(BigDecimal.valueOf(success));
        when(mock.getOngoing()).thenReturn(BigDecimal.valueOf(ongoing));
        when(mock.getFailed()).thenReturn(BigDecimal.valueOf(failed));
        when(mock.getTarget()).thenReturn(BigDecimal.valueOf(target));
        return mock;
    }

    private DeviceTypeBreakdown createDeviceTypeBreakdown() {
        DeviceTypeBreakdown mock = mock(DeviceTypeBreakdown.class);
        when(mock.iterator()).thenReturn(Collections.<TaskStatusBreakdownCounter<DeviceType>>emptyIterator());
        return mock;
    }

    private ConnectionTypeBreakdown createConnectionTypeBreakdown() {
        ConnectionTypeBreakdown mock = mock(ConnectionTypeBreakdown.class);
        when(mock.iterator()).thenReturn(Collections.<TaskStatusBreakdownCounter<ConnectionTypePluggableClass>>emptyIterator());
        return mock;
    }

    private ComPortPoolBreakdown createComPortPoolBreakdown() {
        ComPortPoolBreakdown mock = mock(ComPortPoolBreakdown.class);
        when(mock.getTotalCount()).thenReturn((long) (234+4+411));
        when(mock.getTotalFailedCount()).thenReturn(234L);
        when(mock.getTotalPendingCount()).thenReturn(4L);
        when(mock.getTotalSuccessCount()).thenReturn(411L);
        List<TaskStatusBreakdownCounter<ComPortPool>> counters = new ArrayList<>();
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(1, "Outbound IP"), 11L, 25L, 11L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(2, "Outbound IP post dial"), 411L, 233L, 78L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(3, "Outbound UDP"), 11L, 1233L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(4, "Serial"), 911L, 0L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(5, "Serial PEMP"), 1L, 0L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(6, "Serial PTPP"), 36L, 0L, 0L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(7, "GPRS"), 1036L, 29L, 0L));
        when(mock.iterator()).thenReturn(counters.iterator());
        return mock;
    }

    private ComPortPool mockComPortPool(long id, String name) {
        ComPortPool mock = mock(ComPortPool.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }


}