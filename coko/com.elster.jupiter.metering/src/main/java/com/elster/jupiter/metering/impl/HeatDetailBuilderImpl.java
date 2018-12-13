/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Iterator;

public class HeatDetailBuilderImpl implements HeatDetailBuilder {

    private YesNoAnswer collar = YesNoAnswer.UNKNOWN;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private YesNoAnswer bypass = YesNoAnswer.UNKNOWN;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve = YesNoAnswer.UNKNOWN;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public HeatDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public HeatDetailBuilder withCollar(YesNoAnswer collar) {
        this.collar = collar;
        return this;
    }

    @Override
    public HeatDetailBuilder withPressure(Quantity pressure) {
        this.pressure = pressure;
        return this;
    }

    @Override
    public HeatDetailBuilder withPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
        return this;
    }

    @Override
    public HeatDetailBuilder withBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
        return this;
    }

    @Override
    public HeatDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
        return this;
    }

    @Override
    public HeatDetailBuilder withValve(YesNoAnswer valve) {
        this.valve = valve;
        return this;
    }

    @Override
    public HeatDetail create() {
        HeatDetail detail = buildDetail();
        usagePoint.addDetail(detail);
        return detail;
    }

    @Override
    public void validate() {
        Save.CREATE.validate(dataModel, buildDetail());
    }

    private HeatDetail buildDetail(){
        Range<Instant> newDetailRange = interval.toClosedOpenRange();
        Iterator<? extends UsagePointDetail> iterator = usagePoint.getDetail(newDetailRange).iterator();
        if (iterator.hasNext()) {
            usagePoint.terminateDetail(iterator.next(), newDetailRange.lowerEndpoint());
            if (iterator.hasNext()) {
                interval = Interval.of(Range.closedOpen(newDetailRange.lowerEndpoint(), iterator.next().getRange().lowerEndpoint()));
            }
        }
        HeatDetailImpl detail = dataModel.getInstance(HeatDetailImpl.class).init(usagePoint, interval);
        detail.setCollar(collar);
        detail.setPressure(pressure);
        detail.setPhysicalCapacity(physicalCapacity);
        detail.setBypass(bypass);
        detail.setBypassStatus(bypassStatus);
        detail.setValve(valve);
        return detail;
    }
}
