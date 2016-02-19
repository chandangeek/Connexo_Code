package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public class GasDetailBuilderImpl implements GasDetailBuilder {

    private Optional<Boolean> collar = Optional.empty();
    private Boolean grounded;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private Boolean limiter;
    private String loadLimiterType;
    private Quantity loadLimit;
    private Optional<Boolean> bypass = Optional.empty();
    private BypassStatus bypassStatus;
    private Optional<Boolean> valve = Optional.empty();
    private Optional<Boolean> capped = Optional.empty();
    private Optional<Boolean> clamped = Optional.empty();
    private Boolean interruptible;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public GasDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public GasDetailBuilder withCollar(Boolean collar) {
        this.collar = Optional.ofNullable(collar);
        return this;
    }

    @Override
    public GasDetailBuilder withGrounded(Boolean grounded) {
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
    public GasDetailBuilder withLimiter(Boolean limiter) {
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
    public GasDetailBuilder withBypass(Boolean bypass) {
        this.bypass = Optional.ofNullable(bypass);
        return this;
    }

    @Override
    public GasDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
        return this;
    }

    @Override
    public GasDetailBuilder withValve(Boolean valve) {
        this.valve = Optional.ofNullable(valve);
        return this;
    }

    @Override
    public GasDetailBuilder withCapped(Boolean capped) {
        this.capped = Optional.ofNullable(capped);
        return this;
    }

    @Override
    public GasDetailBuilder withClamped(Boolean clamped) {
        this.clamped = Optional.ofNullable(clamped);
        return this;
    }

    @Override
    public GasDetailBuilder withInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    @Override
    public Optional<Boolean> getCollar() {
        return collar;
    }

    @Override
    public Optional<Boolean> getClamped() {
        return clamped;
    }

    @Override
    public Boolean isGrounded() {
        return grounded;
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
    public Boolean isLimiter() {
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

    @Override
    public Optional<Boolean> getCapped() {
        return capped;
    }

    @Override
    public Boolean isInterruptible() {
        return interruptible;
    }


    @Override
    public GasDetail build() {
        GasDetail gd = dataModel.getInstance(GasDetailImpl.class).init(usagePoint, this, interval);
        usagePoint.addDetail(gd);
        return gd;
    }

}
