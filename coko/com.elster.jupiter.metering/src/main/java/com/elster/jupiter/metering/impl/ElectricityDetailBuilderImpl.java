/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Iterator;

public class ElectricityDetailBuilderImpl implements ElectricityDetailBuilder {

    private YesNoAnswer collar = YesNoAnswer.UNKNOWN;

    private YesNoAnswer grounded;
    private Quantity nominalServiceVoltage;
    private PhaseCode phaseCode;
    private Quantity ratedCurrent;
    private Quantity ratedPower;
    private Quantity estimatedLoad;
    private YesNoAnswer limiter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String loadLimiterType;
    private Quantity loadLimit;
    private YesNoAnswer interruptible;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public ElectricityDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public ElectricityDetailBuilder withCollar(YesNoAnswer collar) {
        this.collar = collar;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withGrounded(YesNoAnswer grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage) {
        this.nominalServiceVoltage = nominalServiceVoltage;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode) {
        this.phaseCode = phaseCode;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withRatedPower(Quantity ratedPower) {
        this.ratedPower = ratedPower;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad) {
        this.estimatedLoad = estimatedLoad;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withLimiter(YesNoAnswer limiter) {
        this.limiter = limiter;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
        return this;
    }

    @Override
    public ElectricityDetailBuilder withInterruptible(YesNoAnswer interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    @Override
    public ElectricityDetail create() {
        ElectricityDetail detail = buildDetail();
        usagePoint.addDetail(detail);
        return detail;
    }

    @Override
    public void validate() {
        Save.CREATE.validate(dataModel,buildDetail());
    }

    private ElectricityDetail buildDetail(){
        Range<Instant> newDetailRange = interval.toClosedOpenRange();
        Iterator<? extends UsagePointDetail> iterator = usagePoint.getDetail(newDetailRange).iterator();
        if (iterator.hasNext()) {
            usagePoint.terminateDetail(iterator.next(), newDetailRange.lowerEndpoint());
            if (iterator.hasNext()) {
                interval = Interval.of(Range.closedOpen(newDetailRange.lowerEndpoint(), iterator.next().getRange().lowerEndpoint()));
            }
        }
        ElectricityDetailImpl ed = dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, interval);
        ed.setCollar(collar);
        ed.setGrounded(grounded);
        ed.setNominalServiceVoltage(nominalServiceVoltage);
        ed.setPhaseCode(phaseCode);
        ed.setRatedCurrent(ratedCurrent);
        ed.setRatedPower(ratedPower);
        ed.setEstimatedLoad(estimatedLoad);
        ed.setLimiter(limiter);
        ed.setLoadLimiterType(loadLimiterType);
        ed.setLoadLimit(loadLimit);
        ed.setInterruptible(interruptible);
        return ed;
    }
}
