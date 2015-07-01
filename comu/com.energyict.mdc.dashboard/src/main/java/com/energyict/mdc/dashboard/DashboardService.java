package com.energyict.mdc.dashboard;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

/**
 * Provides services to dashboard-like information of the
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * and/or {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:28)
 */
@ProviderType
public interface DashboardService {

    public TaskStatusOverview getConnectionTaskStatusOverview();

    public TaskStatusOverview getConnectionTaskStatusOverview(EndDeviceGroup deviceGroup);

    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview();

    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(EndDeviceGroup deviceGroup);

    public ComPortPoolBreakdown getComPortPoolBreakdown();

    public ComPortPoolBreakdown getComPortPoolBreakdown(EndDeviceGroup deviceGroup);

    public ConnectionTypeBreakdown getConnectionTypeBreakdown();

    public ConnectionTypeBreakdown getConnectionTypeBreakdown(EndDeviceGroup deviceGroup);

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown();

    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup);

    public ConnectionTypeHeatMap getConnectionTypeHeatMap();

    public ConnectionTypeHeatMap getConnectionTypeHeatMap(EndDeviceGroup EndDeviceGroup);

    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap();

    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup);

    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap();

    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup);

    public TaskStatusOverview getCommunicationTaskStatusOverview();

    public TaskStatusOverview getCommunicationTaskStatusOverview(EndDeviceGroup deviceGroup);

    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown();

    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup);

    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown();

    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(EndDeviceGroup deviceGroup);

    public ComTaskBreakdown getCommunicationTasksBreakdown();

    public ComTaskBreakdown getCommunicationTasksBreakdown(EndDeviceGroup deviceGroup);

    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview();

    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(EndDeviceGroup deviceGroup);

    public CommunicationTaskHeatMap getCommunicationTasksHeatMap();

    public CommunicationTaskHeatMap getCommunicationTasksHeatMap(EndDeviceGroup deviceGroup);

}