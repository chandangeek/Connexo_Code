package com.energyict.mdc.dashboard;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;

/**
 * Provides services to dashboard-like information of the
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * and/or {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:28)
 */
public interface DashboardService {

    public TaskStatusOverview getConnectionTaskStatusOverview();

    public TaskStatusOverview getConnectionTaskStatusOverview(QueryEndDeviceGroup deviceGroup);

    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview();

    public ComPortPoolBreakdown getComPortPoolBreakdown();

    public ComPortPoolBreakdown getComPortPoolBreakdown(QueryEndDeviceGroup deviceGroup);

    public ConnectionTypeBreakdown getConnectionTypeBreakdown();

    public ConnectionTypeBreakdown getConnectionTypeBreakdown(QueryEndDeviceGroup deviceGroup);

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown();

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(QueryEndDeviceGroup deviceGroup);

    public ConnectionTypeHeatMap getConnectionTypeHeatMap();

    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap();

    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap();

    public TaskStatusOverview getCommunicationTaskStatusOverview();

    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown();

    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown();

    public ComTaskBreakdown getCommunicationTasksBreakdown();

    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview();

    public CommunicationTaskHeatMap getCommunicationTasksHeatMap();

}