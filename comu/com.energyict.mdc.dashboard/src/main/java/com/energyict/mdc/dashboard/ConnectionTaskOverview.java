/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;

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