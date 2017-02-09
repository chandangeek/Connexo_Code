/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.time.Clock;

public class DefaultUsagePointDetailImpl extends UsagePointDetailImpl {

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
}
