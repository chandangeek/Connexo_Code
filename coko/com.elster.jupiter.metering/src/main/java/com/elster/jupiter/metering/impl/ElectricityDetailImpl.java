package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;

public class ElectricityDetailImpl extends UsagePointDetailImpl implements ElectricityDetail {

    private Boolean grounded;
    private Quantity nominalServiceVoltage;
    private PhaseCode phaseCode;
    private Quantity ratedCurrent;
    private Quantity ratedPower;
    private Quantity estimatedLoad;
    private Boolean limiter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String loadLimiterType;
    private Quantity loadLimit;
    private Boolean interruptible;

    @Inject
    ElectricityDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static ElectricityDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, interval);
    }

    ElectricityDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        this.phaseCode = PhaseCode.UNKNOWN;
        return this;
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

    public void setGrounded(Boolean grounded) {
        this.grounded = grounded;
    }

    public void setNominalServiceVoltage(Quantity nominalServiceVoltage) {
        this.nominalServiceVoltage = nominalServiceVoltage;
    }

    public void setPhaseCode(PhaseCode phaseCode) {
        this.phaseCode = phaseCode;
    }

    public void setRatedCurrent(Quantity ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
    }

    public void setRatedPower(Quantity ratedPower) {
        this.ratedPower = ratedPower;
    }

    public void setEstimatedLoad(Quantity estimatedLoad) {
        this.estimatedLoad = estimatedLoad;
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

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }
}
