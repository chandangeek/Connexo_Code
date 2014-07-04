package com.energyict.mdc.scheduling.model;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface ComSchedule extends HasId, DataCollectionConfiguration {

    public long getId();

    public String getName();
    public void setName(String name);

    public SchedulingStatus getSchedulingStatus();
    public void setSchedulingStatus(SchedulingStatus status);

    public Date getNextTimestamp(Calendar calendar);

    public void addComTask(ComTask comTask);
    public void removeComTask(ComTask comTask);

    public List<ComTask> getComTasks();
    public boolean containsComTask (ComTask comTask);

    public NextExecutionSpecs getNextExecutionSpecs();

    public TemporalExpression getTemporalExpression();

    public void setTemporalExpression(TemporalExpression temporalExpression);

    public void save();

    public void delete();

    public UtcInstant getStartDate();

    public void setStartDate(UtcInstant startDate);

    public Date getPlannedDate();

    /**
     * Gets the master resource identifier of this ComSchedule
     * This identifier is typically used by external
     * systems to refer to the ComSchedule and is therefore
     * assigned at creation time and then never changed.
     *
     * @return This ComSchedule's master resource identifier
     */
    public String getmRID();

    public void setmRID(String mRID);

}