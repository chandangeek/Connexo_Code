package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class DefaultUsagePointDetailImpl extends UsagePointDetailImpl{

    @Inject
    DefaultUsagePointDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static DefaultUsagePointDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(DefaultUsagePointDetailImpl.class).init(usagePoint, interval);
    }

    DefaultUsagePointDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }

    DefaultUsagePointDetailImpl init(UsagePoint usagePoint, UsagePointDetailBuilder builder, Interval interval) {
        super.init(usagePoint, builder, interval);
        return this;
    }
}
