package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;

public class GasDetailImpl extends UsagePointDetailImpl implements GasDetail {

    @Inject
    GasDetailImpl(Clock clock) {
        super(clock);
    }

    static GasDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(GasDetailImpl.class).init(usagePoint, interval);
    }

    GasDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }
}
