package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public class GasDetailImpl extends UsagePointDetailImpl implements GasDetail {

    private Boolean grounded;
    private Quantity pressure;
    private Quantity physicalCapacity;
    private Boolean limiter;
    private String loadLimiterType;
    private Quantity loadLimit;
    private Boolean bypass;
    private BypassStatus bypassStatus;
    private Boolean valve;
    private Boolean capped;
    private Boolean clamped;
    private Boolean interruptible;

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

    GasDetailImpl init(UsagePoint usagePoint, GasDetailBuilder builder, Interval interval) {
        super.init(usagePoint, builder, interval);
        this.setGrounded(builder.isGrounded());
        this.setPressure(builder.getPressure());
        this.setPhysicalCapacity(builder.getPhysicalCapacity());
        this.setLimiter(builder.isLimiter());
        this.setLoadLimiterType(builder.getLoadLimiterType());
        this.setLoadLimit(builder.getLoadLimit());
        this.setBypass(builder.getBypass());
        this.setBypassStatus(builder.getBypassStatus());
        this.setValve(builder.getValve());
        this.setCapped(builder.getCapped());
        this.setClamped(builder.getClamped());
        this.setInterruptible(builder.isInterruptible());
        return this;
    }

    @Override
    public Boolean isGrounded() {
        return grounded;
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
    public Optional<Boolean> getCapped() {
        return Optional.ofNullable(capped);
    }

    @Override
    public Optional<Boolean> getClamped() {
        return Optional.ofNullable(clamped);
    }

    @Override
    public Boolean isInterruptible() {
        return interruptible;
    }

    public void setGrounded(Boolean grounded) {
        this.grounded = grounded;
    }

    public void setLimiter(Boolean limiter) {
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

    public void setBypass(Optional<Boolean> bypass) {
        this.bypass = bypass.isPresent() ? bypass.get() : null;
    }

    public void setBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    public void setValve(Optional<Boolean> valve) {
        this.valve = valve.isPresent() ? valve.get() : null;
    }

    public void setCapped(Optional<Boolean> capped) {
        this.capped = capped.isPresent() ? capped.get() : null;
    }

    public void setClamped(Optional<Boolean> clamped) {
        this.clamped = clamped.isPresent() ? clamped.get() : null;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }
}
