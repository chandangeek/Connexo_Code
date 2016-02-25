package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

import java.util.Optional;

public class DefaultUsagePointDetailBuilderImpl implements UsagePointDetailBuilder {

    private Optional<Boolean> collar = Optional.empty();

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public DefaultUsagePointDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public UsagePointDetailBuilder withCollar(Boolean collar) {
        this.collar = Optional.ofNullable(collar);
        return this;
    }

    @Override
    public Optional<Boolean> getCollar() {
        return collar;
    }

    @Override
    public UsagePointDetail create() {
        DefaultUsagePointDetailImpl ed = dataModel.getInstance(DefaultUsagePointDetailImpl.class)
                .init(usagePoint, this, interval);
        usagePoint.addDetail(ed);
        return ed;
    }

    @Override
    public void validate() {
        DefaultUsagePointDetailImpl ed = dataModel.getInstance(DefaultUsagePointDetailImpl.class)
                .init(usagePoint, this, interval);
        Save.CREATE.validate(dataModel, ed);
    }
}
