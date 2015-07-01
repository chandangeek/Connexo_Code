package com.energyict.mdc.scheduling.model;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface ComSchedule extends HasId, HasName, DataCollectionConfiguration {

    public void setName(String name);

    public SchedulingStatus getSchedulingStatus();

    public void setSchedulingStatus(SchedulingStatus status);

    public Date getNextTimestamp(Calendar calendar);

    public void addComTask(ComTask comTask);

    public void removeComTask(ComTask comTask);

    public List<ComTask> getComTasks();

    public boolean containsComTask(ComTask comTask);

    public NextExecutionSpecs getNextExecutionSpecs();

    public TemporalExpression getTemporalExpression();

    public void setTemporalExpression(TemporalExpression temporalExpression);

    public void save();

    public void delete();

    /**
     * Makes this ComSchedule obsolete, i.e. it will appear as it no longer exists.
     */
    public void makeObsolete();

    /**
     * Indicates if this ComSchedule is marked as deleted.
     *
     * @return A flag that indicates if this ComSchedule is marked as deleted
     */
    public boolean isObsolete();

    /**
     * Gets the date when this ComSchedule was made obsolete.
     *
     * @return The date when this ComSchedule was made obsolete
     * or <code>empty</code> when this ComSchedule is not obsolete at all.
     */
    public Optional<Instant> getObsoleteDate();

    public Instant getStartDate();

    public void setStartDate(Instant startDate);

    public Optional<Instant> getPlannedDate();

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