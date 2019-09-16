/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model;

import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ComScheduleBuilder {

    ComScheduleBuilder addComTask(ComTask comTask);

    ComScheduleBuilder mrid(String mrid);

    ComSchedule build();
}
