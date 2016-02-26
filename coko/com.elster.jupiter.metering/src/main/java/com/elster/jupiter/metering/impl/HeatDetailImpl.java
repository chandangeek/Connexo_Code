package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.time.Clock;

public class HeatDetailImpl extends UsagePointDetailImpl implements HeatDetail {

    private Quantity pressure;
    private Quantity physicalCapacity;
    private YesNoAnswer bypass;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve;
    private boolean interruptible;

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
    public YesNoAnswer isBypassInstalled() {
        return bypass;
    }

    @Override
    public BypassStatus getBypassStatus() {
        return bypassStatus;
    }

    @Override
    public YesNoAnswer isValveInstalled() {
        return valve;
    }

    @Override
    public boolean isInterruptible() {
        return interruptible;
    }

    public void setPressure(Quantity pressure) {
        this.pressure = pressure;
    }

    public void setPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    public void setBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
    }

    public void setBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    public void setValve(YesNoAnswer valve) {
        this.valve = valve;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }
}
