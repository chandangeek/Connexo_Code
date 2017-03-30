/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;

public class DefaultUsagePointDetailBuilderImpl implements UsagePointDetailBuilder {

    private YesNoAnswer collar = YesNoAnswer.UNKNOWN;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public DefaultUsagePointDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public UsagePointDetailBuilder withCollar(YesNoAnswer collar) {
        this.collar = collar;
        return this;
    }

    @Override
    public UsagePointDetail create() {
        DefaultUsagePointDetailImpl ed = dataModel.getInstance(DefaultUsagePointDetailImpl.class)
                .init(usagePoint, interval);
        ed.setCollar(collar);
        usagePoint.addDetail(ed);
        return ed;
    }

    @Override
    public void validate() {
        DefaultUsagePointDetailImpl ed = dataModel.getInstance(DefaultUsagePointDetailImpl.class)
                .init(usagePoint, interval);
        ed.setCollar(collar);
        Save.CREATE.validate(dataModel, ed);
    }
}
