package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;

public class WaterDetailImpl extends UsagePointDetailImpl implements WaterDetail {

    @Inject
    WaterDetailImpl(Clock clock) {
        super(clock);
    }

    static WaterDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, interval);
    }

    WaterDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }
}
