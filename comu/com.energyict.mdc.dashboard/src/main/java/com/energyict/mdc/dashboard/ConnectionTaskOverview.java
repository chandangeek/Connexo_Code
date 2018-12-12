/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides an overview of {@link ConnectionTask}s,
 * counting them by {@link TaskStatus}, {@link ComPortPool},
 * {@link ConnectionTypePluggableClass}, {@link DeviceType} and {@link ComSession.SuccessIndicator}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (14:23)
 */
@ProviderType
public interface ConnectionTaskOverview {

    TaskStatusOverview getTaskStatusOverview();

    ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview();

    ComPortPoolBreakdown getComPortPoolBreakdown();

    ConnectionTypeBreakdown getConnectionTypeBreakdown();

    DeviceTypeBreakdown getDeviceTypeBreakdown();

}