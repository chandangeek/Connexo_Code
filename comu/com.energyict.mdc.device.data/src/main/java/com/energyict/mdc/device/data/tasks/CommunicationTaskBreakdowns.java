/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.CompletionCode;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

/**
 * Provides an overview of {@link ComTaskExecution}s,
 * counting them by {@link TaskStatus}, {@link ComSchedule},
 * {@link ComTask}, {@link DeviceType} and {@link CompletionCode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-21 (16:24)
 */
@ProviderType
public interface CommunicationTaskBreakdowns {

    Map<TaskStatus, Long> getStatusBreakdown();

    Map<ComSchedule, Map<TaskStatus, Long>> getComScheduleBreakdown();

    Map<ComTask, Map<TaskStatus, Long>> getComTaskBreakdown();

    Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown();

}