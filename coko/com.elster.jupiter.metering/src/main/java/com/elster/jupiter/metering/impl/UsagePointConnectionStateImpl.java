/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;


import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Objects;

public class UsagePointConnectionStateImpl implements UsagePointConnectionState {

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();

    private ConnectionState connectionState;

    private Interval interval;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    public UsagePointConnectionStateImpl() {
    }

    public UsagePointConnectionStateImpl initialize(Interval interval, UsagePoint usagePoint, ConnectionState state) {
        this.usagePoint.set(Objects.requireNonNull(usagePoint));
        this.connectionState = Objects.requireNonNull(state);
        this.interval = Objects.requireNonNull(interval);
        return this;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint.get();
    }

    @Override
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate) || interval.toClosedOpenRange().hasUpperBound()) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
    }

    @Override
    public Interval getInterval() {
        return interval;
    }
}
