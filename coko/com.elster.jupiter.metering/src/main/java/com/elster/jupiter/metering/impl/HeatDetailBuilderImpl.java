package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public class HeatDetailBuilderImpl implements HeatDetailBuilder {

    private Optional<Boolean> collar = Optional.empty();
    private Quantity pressure;
    private Quantity physicalCapacity;
    private Optional<Boolean> bypass = Optional.empty();
    private BypassStatus bypassStatus;
    private Optional<Boolean> valve = Optional.empty();
    private Boolean interruptible;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public HeatDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public HeatDetailBuilder withCollar(Boolean collar) {
        this.collar = Optional.ofNullable(collar);
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
    public HeatDetailBuilder withBypass(Boolean bypass) {
        this.bypass = Optional.ofNullable(bypass);
        return this;
    }

    @Override
    public HeatDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
        return this;
    }

    @Override
    public HeatDetailBuilder withValve(Boolean valve) {
        this.valve = Optional.ofNullable(valve);
        return this;
    }

    @Override
    public HeatDetailBuilder withInterruptible(Boolean interruptible) {
        this.valve = Optional.ofNullable(interruptible);
        return this;
    }

    @Override
    public Optional<Boolean> getCollar() {
        return collar;
    }

    @Override
    public Quantity getPressure() {
        return pressure;
    }

    @Override
    public Quantity getPhysicalCapacity() {
        return physicalCapacity;
    }

    @Override
    public Optional<Boolean> getBypass() {
        return bypass;
    }

    @Override
    public BypassStatus getBypassStatus() {
        return bypassStatus;
    }

    @Override
    public Optional<Boolean> getValve() {
        return valve;
    }

    public Boolean isInterruptible() {
        return interruptible;
    }

    @Override
    public HeatDetail build() {
        HeatDetail hd = dataModel.getInstance(HeatDetailImpl.class).init(usagePoint, this, interval);
        usagePoint.addDetail(hd);
        return hd;
    }

    @Override
    public void validate() {
        HeatDetail hd = dataModel.getInstance(HeatDetailImpl.class).init(usagePoint, this, interval);
        Save.CREATE.validate(dataModel, hd);
    }
}
