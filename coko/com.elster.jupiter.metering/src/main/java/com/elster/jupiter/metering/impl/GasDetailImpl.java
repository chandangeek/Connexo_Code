package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;

public class GasDetailImpl extends UsagePointDetailImpl implements GasDetail {

    private boolean grounded;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private boolean limiter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String loadLimiterType;
    private Quantity loadLimit;
    private YesNoAnswer bypass;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve;
    private YesNoAnswer capped;
    private YesNoAnswer clamped;
    private boolean interruptible;

    @Inject
    GasDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static GasDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(GasDetailImpl.class).init(usagePoint, interval);
    }

    GasDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }


    @Override
    public boolean isGrounded() {
        return grounded;
    }

    @Override
    public boolean isLimiter() {
        return limiter;
    }

    @Override
    public String getLoadLimiterType() {
        return loadLimiterType;
    }

    @Override
    public Quantity getLoadLimit() {
        return loadLimit;
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
    public YesNoAnswer isCapped() {
        return capped;
    }

    @Override
    public YesNoAnswer isClamped() {
        return clamped;
    }

    @Override
    public boolean isInterruptible() {
        return interruptible;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void setLimiter(boolean limiter) {
        this.limiter = limiter;
    }

    public void setLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
    }

    public void setLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
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

    public void setCapped(YesNoAnswer capped) {
        this.capped = capped;
    }

    public void setClamped(YesNoAnswer clamped) {
        this.clamped = clamped;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }
}
