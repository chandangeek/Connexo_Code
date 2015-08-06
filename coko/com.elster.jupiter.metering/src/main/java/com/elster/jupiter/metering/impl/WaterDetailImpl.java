package com.elster.jupiter.metering.impl;

import java.time.Clock;

import javax.inject.Inject;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

public class WaterDetailImpl extends UsagePointDetailImpl implements WaterDetail {

    @Inject
    WaterDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static WaterDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, interval);
    }

    WaterDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }

    WaterDetailImpl init(UsagePoint usagePoint, WaterDetailBuilder builder, Interval interval) {
        super.init(usagePoint, builder, interval);
        return this;
    }
}
