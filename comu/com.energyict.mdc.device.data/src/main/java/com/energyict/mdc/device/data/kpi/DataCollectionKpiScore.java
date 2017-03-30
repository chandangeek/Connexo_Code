/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Models scoring of the Kpi at a certain point in time.
 * Will contain scored values for:
 * <ul>
 * <li>Success: {@link TaskStatus#Waiting}</li>
 * <li>Ongoing: {@link TaskStatus#Pending} or {@link TaskStatus#Busy} or {@link TaskStatus#Retrying}</li>
 * <li>Failed: {@link TaskStatus#Failed} or {@link TaskStatus#NeverCompleted}</li>
 * </ul>
 * The natural sort order is determined by the timestamp.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (11:51)
 */
@ProviderType
public interface DataCollectionKpiScore extends Comparable<DataCollectionKpiScore> {

    /**
     * Returns the timestamp of this score.
     *
     * @return The timestamp of this score
     */
    public Instant getTimestamp();

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

    public BigDecimal getSuccess();

    public BigDecimal getOngoing();

    public BigDecimal getFailed();

}