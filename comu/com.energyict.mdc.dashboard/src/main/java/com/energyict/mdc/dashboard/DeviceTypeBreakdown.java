/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the break down of the execution of tasks by {@link DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:30)
 */
@ProviderType
public interface DeviceTypeBreakdown extends TaskStatusBreakdownCounters<DeviceType> {
}