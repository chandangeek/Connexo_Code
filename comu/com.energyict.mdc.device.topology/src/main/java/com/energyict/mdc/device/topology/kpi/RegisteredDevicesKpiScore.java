/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.kpi;


import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Models scoring of the Kpi at a certain point in time.
 * Will contain scored values for:
 * <ul>
 * <li>Total devices in device group}</li>
 * <li>Total registered devices in device group}</li>
 * </ul>
 * The natural sort order is determined by the timestamp.
 */
@ProviderType
public interface RegisteredDevicesKpiScore extends Comparable<RegisteredDevicesKpiScore> {

    /**
     * Returns the timestamp of this score.
     *
     * @return The timestamp of this score
     */
    Instant getTimestamp();

    BigDecimal getTotal();

    BigDecimal getRegistered();

    long getTarget(long target);
}