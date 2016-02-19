package com.elster.jupiter.metering.impl;

import java.time.Clock;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

public class WaterDetailImpl extends UsagePointDetailImpl implements WaterDetail {

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

    @Inject
    WaterDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static WaterDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, interval);
    }

    WaterDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
    }

    WaterDetailImpl init(UsagePoint usagePoint, WaterDetailBuilder builder, Interval interval) {
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
    public void setGrounded(Boolean grounded) {
        this.grounded = grounded;
    }


    @Override
    public void setLimiter(Boolean limiter) {
        this.limiter = limiter;
    }

    @Override
    public void setLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
    }

    @Override
    public void setLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
    }

    @Override
    public void setPressure(Quantity pressure) {
        this.pressure = pressure;
    }

    @Override
    public void setPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    @Override
    public void setBypass(Optional<Boolean> bypass) {
        this.bypass = bypass.isPresent() ? bypass.get() : null;
    }

    @Override
    public void setBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    @Override
    public void setValve(Optional<Boolean> valve) {
        this.valve = valve.isPresent() ? valve.get() : null;
    }

    @Override
    public void setCapped(Optional<Boolean> capped) {
        this.capped = capped.isPresent() ? capped.get() : null;
    }

    @Override
    public void setClamped(Optional<Boolean> clamped) {
        this.clamped = clamped.isPresent() ? clamped.get() : null;
    }
}
