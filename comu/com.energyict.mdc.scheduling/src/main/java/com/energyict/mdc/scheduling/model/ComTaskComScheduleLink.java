/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model;

import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ComTaskComScheduleLink {
    public ComSchedule getComSchedule();
    public ComTask getComTask();
}
