/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

/**
 * Provides an overview of {@link ConnectionTask}s,
 * counting them by {@link TaskStatus}, {@link ComPortPool},
 * {@link ConnectionTypePluggableClass} and {@link DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (11:37)
 */
@ProviderType
public interface ConnectionTaskBreakdowns {

    Map<TaskStatus, Long> getStatusBreakdown();

    Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown();

    Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown();

    Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown();

}