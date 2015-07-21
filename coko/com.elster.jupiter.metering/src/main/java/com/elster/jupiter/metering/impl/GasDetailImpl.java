package com.elster.jupiter.metering.impl;

import java.time.Clock;

import javax.inject.Inject;

import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

public class GasDetailImpl extends UsagePointDetailImpl implements GasDetail {

    @Inject
    GasDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static GasDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(GasDetailImpl.class).init(usagePoint, interval);
    }

    GasDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }
    
    GasDetailImpl init(UsagePoint usagePoint, GasDetailBuilder builder, Interval interval) {
        super.init(usagePoint, builder, interval);
        return this;
    }
}
