package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;

public class UsagePointStateTemporalImpl implements Effectivity {

    private final DataModel dataModel;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<UsagePointState> state = ValueReference.absent();
    private Interval interval;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    public UsagePointStateTemporalImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    UsagePointStateTemporalImpl init(UsagePointImpl usagePoint, UsagePointState state, Instant startTime) {
        this.usagePoint.set(usagePoint);
        this.state.set(state);
        this.interval = Interval.of(Range.atLeast(startTime));
        return this;
    }

    public UsagePointState getState() {
        return this.state.get();
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    public void close(Instant closingTime) {
        if (!isEffectiveAt(closingTime) || this.interval.toClosedOpenRange().hasUpperBound()) {
            throw new IllegalArgumentException("Incorrect usage point's state interval.");
        }
        this.interval = this.interval.withEnd(closingTime);
        this.dataModel.update(this);
    }
}
