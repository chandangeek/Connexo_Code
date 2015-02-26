package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.TimeZone;

/**
 * A Kpi models a set of metrics (key performance indicators) to be monitored at set interval lengths.
 */
public interface Kpi extends HasName {

    /**
     * @return the database id of this Kpi
     */
    long getId();

    /**
     * @return a list containing all KpiMembers of this Kpi
     */
    List<? extends KpiMember> getMembers();

    /**
     * @return the TimeZone this Kpi works against.
     */
    TimeZone getTimeZone();

    /**
     * @return the IntervalLength between each recording of a kpi score.
     */
    TemporalAmount getIntervalLength();

    /**
     * Saves this Kpi to DB.
     */
    void save();

    /**
     * Removes this Kpi from the DB
     */
    void remove();
}
