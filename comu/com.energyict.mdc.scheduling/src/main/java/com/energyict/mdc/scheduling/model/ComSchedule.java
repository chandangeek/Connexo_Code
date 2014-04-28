package com.energyict.mdc.scheduling.model;

import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;
import java.util.List;

public interface ComSchedule {

    public long getId();

    public String getName();
    public void setName(String name);

    public SchedulingStatus getSchedulingStatus();

    public void addComTask(ComTask comTask);

    public List<ComTask> getComTasks();

    public TemporalExpression getTemporalExpression();

    public void setTemporalExpression(TemporalExpression temporalExpression);

    public void save();

    public void delete();
}
