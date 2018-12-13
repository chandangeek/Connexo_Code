/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiScore;

import java.math.BigDecimal;
import java.time.Instant;

public class RegisteredDevicesKpiScoreImpl implements RegisteredDevicesKpiScore {
    private final Instant timestamp;
    private BigDecimal total;
    private final BigDecimal registered;

    public RegisteredDevicesKpiScoreImpl(Instant timestamp, BigDecimal total, BigDecimal registered) {
        this.timestamp = timestamp;
        this.total = total;
        this.registered = registered;
    }

    public RegisteredDevicesKpiScoreImpl(Instant timestamp, BigDecimal registered) {
        this.timestamp = timestamp;
        this.registered = registered;
    }


    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public BigDecimal getRegistered() {
        return registered;
    }

    @Override
    public long getTarget(long target) {
        return Math.round(getTotal().divide(BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(target)).doubleValue());
    }

    @Override
    public int compareTo(RegisteredDevicesKpiScore other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RegisteredDevicesKpiScoreImpl that = (RegisteredDevicesKpiScoreImpl) other;
        return this.timestamp.equals(that.timestamp);
    }
}
