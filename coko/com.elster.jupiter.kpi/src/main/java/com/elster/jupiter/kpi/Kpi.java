/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * A Kpi models a set of metrics (key performance indicators) to be monitored at set interval lengths.
 */
@ProviderType
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
     * Removes this Kpi from the DB
     */
    void remove();

    /**
     * Stores scores for the passed members
     *
     * @param memberScores the map of scores per member
     * @since 2.2
     */
    void store(Map<KpiMember, Map<Instant, BigDecimal>> memberScores);

    /**
     * Starts the update of this Kpi
     *
     * @return an updater
     */
    KpiUpdater startUpdate();
}
