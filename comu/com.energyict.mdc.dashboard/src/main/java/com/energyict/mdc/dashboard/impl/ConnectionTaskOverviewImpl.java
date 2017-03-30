/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;

/**
 * Provides an implementation for the {@link ConnectionTaskOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (14:27)
 */
class ConnectionTaskOverviewImpl implements ConnectionTaskOverview {

    private final TaskStatusOverview overview;
    private final ComSessionSuccessIndicatorOverview lastResultsOverview;
    private final ComPortPoolBreakdown comPortPoolBreakdown;
    private final ConnectionTypeBreakdown connectionTypeBreakdown;
    private final DeviceTypeBreakdown deviceTypeBreakdown;

    ConnectionTaskOverviewImpl(ConnectionTaskBreakdowns breakdowns, ComSessionSuccessIndicatorOverview lastResultsOverview) {
        this.overview = TaskStatusOverviewImpl.from(breakdowns.getStatusBreakdown());
        this.lastResultsOverview = lastResultsOverview;
        this.comPortPoolBreakdown = ComPortPoolBreakdownImpl.from(breakdowns.getComPortPoolBreakdown());
        this.connectionTypeBreakdown = ConnectionTypeBreakdownImpl.from(breakdowns.getConnectionTypeBreakdown());
        this.deviceTypeBreakdown = DeviceTypeBreakdownImpl.from(breakdowns.getDeviceTypeBreakdown());
    }

    @Override
    public TaskStatusOverview getTaskStatusOverview() {
        return this.overview;
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview() {
        return this.lastResultsOverview;
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown() {
        return this.comPortPoolBreakdown;
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown() {
        return this.connectionTypeBreakdown;
    }

    @Override
    public DeviceTypeBreakdown getDeviceTypeBreakdown() {
        return this.deviceTypeBreakdown;
    }

}