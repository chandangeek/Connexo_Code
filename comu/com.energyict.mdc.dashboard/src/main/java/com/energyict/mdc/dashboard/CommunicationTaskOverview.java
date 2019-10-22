/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.CompletionCode;

import aQute.bnd.annotation.ProviderType;

/**
 * Provides an overview of {@link ComTaskExecution}s,
 * counting them by {@link TaskStatus}, {@link ComSchedule},
 * {@link ComTask}, {@link DeviceType} and {@link CompletionCode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-21 (16:53)
 */
@ProviderType
public interface CommunicationTaskOverview {

    TaskStatusOverview getStatusOverview();

    ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview();

    ComScheduleBreakdown getComScheduleBreakdown();

    ComTaskBreakdown getComTaskBreakdown();

    DeviceTypeBreakdown getDeviceTypeBreakdown();

    CommunicationTaskHeatMap getHeatMap();

}