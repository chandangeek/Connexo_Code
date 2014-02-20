package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.DefaultDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;

public class DefaultDetailImpl extends UsagePointDetailImpl implements DefaultDetail {

    @Inject
    DefaultDetailImpl(Clock clock) {
        super(clock);
    }

    static DefaultDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(DefaultDetailImpl.class).init(usagePoint, interval);
    }

    DefaultDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }
}
