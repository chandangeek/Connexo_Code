package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.scheduling.model.ComSchedule;

public interface ScheduledComTaskExecution extends ComTaskExecution {
    public ComSchedule getComSchedule();
}
