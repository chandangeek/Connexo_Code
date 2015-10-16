package com.energyict.mdc.scheduling.model;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.tasks.ComTask;

@ProviderType
public interface ComScheduleBuilder {

    ComScheduleBuilder addComTask(ComTask comTask);

    ComScheduleBuilder mrid(String mrid);

    ComSchedule build();
}
