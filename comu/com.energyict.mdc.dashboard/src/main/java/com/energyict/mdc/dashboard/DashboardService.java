/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;

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

    TaskStatusOverview getConnectionTaskStatusOverview();

    TaskStatusOverview getConnectionTaskStatusOverview(EndDeviceGroup deviceGroup);

    ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview();

    ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(EndDeviceGroup deviceGroup);

    ComPortPoolBreakdown getComPortPoolBreakdown();

    ComPortPoolBreakdown getComPortPoolBreakdown(EndDeviceGroup deviceGroup);

    ConnectionTypeBreakdown getConnectionTypeBreakdown();

    ConnectionTypeBreakdown getConnectionTypeBreakdown(EndDeviceGroup deviceGroup);

    DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown();

    DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup);

    ConnectionTypeHeatMap getConnectionTypeHeatMap();

    ConnectionTypeHeatMap getConnectionTypeHeatMap(EndDeviceGroup EndDeviceGroup);

    ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap();

    ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup);

    ComPortPoolHeatMap getConnectionsComPortPoolHeatMap();

    ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup);

    TaskStatusOverview getCommunicationTaskStatusOverview();

    TaskStatusOverview getCommunicationTaskStatusOverview(EndDeviceGroup deviceGroup);

    DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown();

    DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup);

    ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown();

    ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(EndDeviceGroup deviceGroup);

    ComTaskBreakdown getCommunicationTasksBreakdown();

    ComTaskBreakdown getCommunicationTasksBreakdown(EndDeviceGroup deviceGroup);

    ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview();

    ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(EndDeviceGroup deviceGroup);

    CommunicationTaskHeatMap getCommunicationTasksHeatMap();

    CommunicationTaskHeatMap getCommunicationTasksHeatMap(EndDeviceGroup deviceGroup);

    CommunicationTaskOverview getCommunicationTaskOverview();

    CommunicationTaskOverview getCommunicationTaskOverview(EndDeviceGroup deviceGroup);

    ConnectionTaskOverview getConnectionTaskOverview();

    ConnectionTaskOverview getConnectionTaskOverview(EndDeviceGroup deviceGroup);

}