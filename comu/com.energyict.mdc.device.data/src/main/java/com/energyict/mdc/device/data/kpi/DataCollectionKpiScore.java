package com.energyict.mdc.device.data.kpi;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Models scoring of the Kpi at a certain point in time.
 * Will contain scored values for all the monitored {@link TaskStatus}ses.
 * The complete list of monitored TaskStatusses is documented
 * {@link DataCollectionKpiService#MONITORED_STATUSSES here}.
 * The natural sort order is determined by the timestamp.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (11:51)
 */
public interface DataCollectionKpiScore extends Comparable<DataCollectionKpiScore> {

    /**
     * Returns the timestamp of this score.
     *
     * @return The timestamp of this score
     */
    public Date getTimestamp();

    /**
     * Returns the target of the related {@link DataCollectionKpi}.
     *
     * @return The target
     */
    public BigDecimal getTarget();

    /**
     * Tests if this score meets the target.
     *
     * @return A flag that indicates if this score meets the target
     */
    public boolean meetsTarget();

    /**
     * Gets the scored value for the specified {@link TaskStatus}.
     *
     * @param status The TaskStatus
     * @return The scored value
     */
    public BigDecimal getValue(TaskStatus status);

}