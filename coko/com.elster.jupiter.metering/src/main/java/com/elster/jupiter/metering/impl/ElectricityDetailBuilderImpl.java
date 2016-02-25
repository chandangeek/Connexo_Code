package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public class ElectricityDetailBuilderImpl implements ElectricityDetailBuilder {

    private Optional<Boolean> collar = Optional.empty();

    private Boolean grounded;
    private Quantity nominalServiceVoltage;
    private PhaseCode phaseCode;
    private Quantity ratedCurrent;
    private Quantity ratedPower;
    private Quantity estimatedLoad;
    private Boolean limiter;
    private String loadLimiterType;
    private Quantity loadLimit;
    private Boolean interruptible;

    private UsagePoint usagePoint;
    private Interval interval;
    private DataModel dataModel;

    public ElectricityDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
        this.dataModel = dataModel;
        this.usagePoint = usagePointImpl;
        this.interval = interval;
    }

    @Override
    public ElectricityDetailBuilder withCollar(Boolean collar) {
        this.collar = Optional.ofNullable(collar);
        return this;
    }

    @Override
    public ElectricityDetailBuilder withGrounded(Boolean grounded) {
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
    public ElectricityDetailBuilder withLimiter(Boolean limiter) {
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
    public ElectricityDetailBuilder withInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    @Override
    public Optional<Boolean> getCollar() {
        return collar;
    }

    @Override
    public Boolean isGrounded() {
        return grounded;
    }

    @Override
    public Quantity getNominalServiceVoltage() {
        return nominalServiceVoltage;
    }

    @Override
    public PhaseCode getPhaseCode() {
        return phaseCode;
    }

    @Override
    public Quantity getRatedCurrent() {
        return ratedCurrent;
    }

    @Override
    public Quantity getRatedPower() {
        return ratedPower;
    }

    @Override
    public Quantity getEstimatedLoad() {
        return estimatedLoad;
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
    public Boolean isInterruptible() {
        return interruptible;
    }

    @Override
    public ElectricityDetail create() {
        ElectricityDetail ed = dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, this, interval);
        usagePoint.addDetail(ed);
        return ed;
    }

    @Override
    public void validate() {
        ElectricityDetail ed = dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, this, interval);
        Save.CREATE.validate(dataModel,ed);
    }
}
