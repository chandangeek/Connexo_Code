package com.energyict.mdc.scheduling.model;

import com.energyict.mdc.tasks.ComTask;

public interface ComTaskComScheduleLink {
    public ComSchedule getComSchedule();
    public ComTask getComTask();
}
