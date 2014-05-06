package com.energyict.mdc.scheduling.model;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface ComSchedule {

    public long getId();

    public String getName();
    public void setName(String name);

    public SchedulingStatus getSchedulingStatus();
    public void setSchedulingStatus(SchedulingStatus status);


    public Date getNextTimestamp(Calendar calendar);

    public void addComTask(ComTask comTask);

    public List<ComTask> getComTasks();

    public TemporalExpression getTemporalExpression();

    public void setTemporalExpression(TemporalExpression temporalExpression);

    public void save();

    public void delete();

    public UtcInstant getStartDate();

    public void setStartDate(UtcInstant startDate);

    Date getPlannedDate();
}
