/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;


import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public class UsagePointConnectionStateImpl implements UsagePointConnectionState {

    @IsPresent
    private Reference<UsagePointImpl> usagePoint = ValueReference.absent();

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

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointConnectionStateImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public UsagePointConnectionStateImpl init(UsagePointImpl usagePoint, ConnectionState state, Range<Instant> interval) {
        this.usagePoint.set(Objects.requireNonNull(usagePoint));
        this.connectionState = Objects.requireNonNull(state);
        this.interval = Interval.of(interval);
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
    public String getConnectionStateDisplayName() {
        return this.thesaurus.getFormat(this.connectionState).format();
    }

    @Deprecated
    @Override
    public void close(Instant closingDate) {
        // nothing to do
    }

    public void endAt(Instant closingDate) {
        if (!isEffectiveAt(closingDate) || interval.toClosedOpenRange().hasUpperBound()) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
        this.dataModel.update(this);
    }

    @Override
    public Interval getInterval() {
        return interval;
    }
}
