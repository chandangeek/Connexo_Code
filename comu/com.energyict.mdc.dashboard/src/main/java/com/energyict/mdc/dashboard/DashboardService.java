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

    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(QueryEndDeviceGroup deviceGroup);

    public ComPortPoolBreakdown getComPortPoolBreakdown();

    public ComPortPoolBreakdown getComPortPoolBreakdown(QueryEndDeviceGroup deviceGroup);

    public ConnectionTypeBreakdown getConnectionTypeBreakdown();

    public ConnectionTypeBreakdown getConnectionTypeBreakdown(QueryEndDeviceGroup deviceGroup);

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown();

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(QueryEndDeviceGroup deviceGroup);

    public ConnectionTypeHeatMap getConnectionTypeHeatMap();

    public ConnectionTypeHeatMap getConnectionTypeHeatMap(QueryEndDeviceGroup queryEndDeviceGroup);

    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap();

    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(QueryEndDeviceGroup deviceGroup);

    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap();

    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(QueryEndDeviceGroup deviceGroup);

    public TaskStatusOverview getCommunicationTaskStatusOverview();

    public TaskStatusOverview getCommunicationTaskStatusOverview(QueryEndDeviceGroup deviceGroup);

    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown();

    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(QueryEndDeviceGroup deviceGroup);

    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown();

    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(QueryEndDeviceGroup deviceGroup);

    public ComTaskBreakdown getCommunicationTasksBreakdown();

    public ComTaskBreakdown getCommunicationTasksBreakdown(QueryEndDeviceGroup deviceGroup);

    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview();

    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(QueryEndDeviceGroup deviceGroup);

    public CommunicationTaskHeatMap getCommunicationTasksHeatMap();

    public CommunicationTaskHeatMap getCommunicationTasksHeatMap(QueryEndDeviceGroup deviceGroup);

}