package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class HeatDetailImpl extends UsagePointDetailImpl implements HeatDetail {

    private Quantity pressure;
    private Quantity physicalCapacity;
    private Boolean bypass;
    private BypassStatus bypassStatus;
    private Boolean valve;
    private Boolean interruptible;

    @Inject
    HeatDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static HeatDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(HeatDetailImpl.class).init(usagePoint, interval);
    }

    HeatDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }

    @Override
    public Quantity getPhysicalCapacity() {
        return physicalCapacity;
    }

    @Override
    public Quantity getPressure() {
        return pressure;
    }

    @Override
    public Optional<Boolean> getBypass() {
        return Optional.ofNullable(bypass);
    }

    @Override
    public BypassStatus getBypassStatus() {
        return bypassStatus;
    }

    @Override
    public Optional<Boolean> getValve() {
        return Optional.ofNullable(valve);
    }

    @Override
    public Boolean isInterruptible() {
        return interruptible;
    }

    public void setPressure(Quantity pressure) {
        this.pressure = pressure;
    }

    public void setPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    public void setBypass(Optional<Boolean> bypass) {
        this.bypass = bypass.isPresent() ? bypass.get() : null;
    }

    public void setBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    public void setValve(Optional<Boolean> valve) {
        this.valve = valve.isPresent() ? valve.get() : null;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }
}
