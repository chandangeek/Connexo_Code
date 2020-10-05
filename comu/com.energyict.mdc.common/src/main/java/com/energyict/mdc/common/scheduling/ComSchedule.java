/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.scheduling;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ConsumerType
public interface ComSchedule extends HasId, HasName, DataCollectionConfiguration {

    void setName(String name);

    long getVersion();

    boolean isDefault();

    /**
     * Sets the current device configuration as default on a device type.
     * Sets the old configuration default status to false
     * @param value true if setAsDefault, false if removeAsDefault
     */
    void setDefaultStatus(boolean value);

    SchedulingStatus getSchedulingStatus();

    void setSchedulingStatus(SchedulingStatus status);

    Date getNextTimestamp(Calendar calendar);

    void addComTask(ComTask comTask);

    void removeComTask(ComTask comTask);

    List<ComTask> getComTasks();

    boolean containsComTask(ComTask comTask);

    NextExecutionSpecs getNextExecutionSpecs();

    TemporalExpression getTemporalExpression();

    void setTemporalExpression(TemporalExpression temporalExpression);

    void update();

    void delete();

    /**
     * Makes this ComSchedule obsolete, i.e. it will appear as it no longer exists.
     */
    void makeObsolete();

    /**
     * Indicates if this ComSchedule is marked as deleted.
     *
     * @return A flag that indicates if this ComSchedule is marked as deleted
     */
    boolean isObsolete();

    /**
     * Gets the date when this ComSchedule was made obsolete.
     *
     * @return The date when this ComSchedule was made obsolete
     * or <code>empty</code> when this ComSchedule is not obsolete at all.
     */
    Optional<Instant> getObsoleteDate();

    Instant getStartDate();

    void setStartDate(Instant startDate);

    Optional<Instant> getPlannedDate();

    /**
     * Gets the master resource identifier of this ComSchedule
     * This identifier is typically used by external
     * systems to refer to the ComSchedule and is therefore
     * assigned at creation time and then never changed.
     *
     * @return This ComSchedule's master resource identifier
     */
    Optional<String> getmRID();

    void setmRID(String mRID);

}