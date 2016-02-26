package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

public class GasDetailBuilderImpl implements GasDetailBuilder {

    private YesNoAnswer collar = YesNoAnswer.UNKNOWN;
    private boolean grounded;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private boolean limiter;
    private String loadLimiterType;
    private Quantity loadLimit;
    private YesNoAnswer bypass = YesNoAnswer.UNKNOWN;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve = YesNoAnswer.UNKNOWN;
    private YesNoAnswer capped = YesNoAnswer.UNKNOWN;
    private YesNoAnswer clamped = YesNoAnswer.UNKNOWN;
    private boolean interruptible;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public GasDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public GasDetailBuilder withCollar(YesNoAnswer collar) {
        this.collar = collar;
        return this;
    }

    @Override
    public GasDetailBuilder withGrounded(boolean grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public GasDetailBuilder withPressure(Quantity pressure) {
        this.pressure = pressure;
        return this;
    }

    @Override
    public GasDetailBuilder withPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
        return this;
    }

    @Override
    public GasDetailBuilder withLimiter(boolean limiter) {
        this.limiter = limiter;
        return this;
    }

    @Override
    public GasDetailBuilder withLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
        return this;
    }

    @Override
    public GasDetailBuilder withLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
        return this;
    }

    @Override
    public GasDetailBuilder withBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
        return this;
    }

    @Override
    public GasDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
        return this;
    }

    @Override
    public GasDetailBuilder withValve(YesNoAnswer valve) {
        this.valve = valve;
        return this;
    }

    @Override
    public GasDetailBuilder withCap(YesNoAnswer capped) {
        this.capped = capped;
        return this;
    }

    @Override
    public GasDetailBuilder withClamp(YesNoAnswer clamped) {
        this.clamped = clamped;
        return this;
    }

    @Override
    public GasDetailBuilder withInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    @Override
    public GasDetail create() {
        GasDetail detail = buildDetail();
        usagePoint.addDetail(detail);
        return detail;
    }

    @Override
    public void validate() {
        Save.CREATE.validate(dataModel, buildDetail());
    }

    private GasDetail buildDetail(){
        GasDetailImpl detail = dataModel.getInstance(GasDetailImpl.class).init(usagePoint, interval);
        detail.setCollar(collar);
        detail.setGrounded(grounded);
        detail.setPressure(pressure);
        detail.setPhysicalCapacity(physicalCapacity);
        detail.setLimiter(limiter);
        detail.setLoadLimiterType(loadLimiterType);
        detail.setLoadLimit(loadLimit);
        detail.setBypass(bypass);
        detail.setBypassStatus(bypassStatus);
        detail.setValve(valve);
        detail.setCapped(capped);
        detail.setClamped(clamped);
        detail.setInterruptible(interruptible);
        return detail;
    }
}
