package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.units.Quantity;

import java.time.Clock;

public class ElectricityUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public Boolean grounded;
    public Quantity nominalServiceVoltage;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public Quantity estimatedLoad;
    public Boolean limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public Boolean interruptible;

    public ElectricityUsagePointDetailsInfo() {
    }

    public ElectricityUsagePointDetailsInfo(ElectricityDetail detail) {
        super(detail);
        this.grounded = detail.isGrounded();
        this.nominalServiceVoltage = detail.getNominalServiceVoltage();
        this.phaseCode = detail.getPhaseCode();
        this.ratedCurrent = detail.getRatedCurrent();
        this.ratedPower = detail.getRatedPower();
        this.estimatedLoad = detail.getEstimatedLoad();
        this.limiter = detail.isLimiter();
        this.loadLimiterType = detail.getLoadLimiterType();
        this.loadLimit = detail.getLoadLimit();
        this.interruptible = detail.isInterruptible();
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newElectricityDetailBuilder(clock.instant())
                .withCollar(collar)
                .withGrounded(grounded)
                .withNominalServiceVoltage(nominalServiceVoltage)
                .withPhaseCode(phaseCode)
                .withRatedCurrent(ratedCurrent)
                .withRatedPower(ratedPower)
                .withEstimatedLoad(estimatedLoad)
                .withLimiter(limiter)
                .withLoadLimiterType(loadLimiterType)
                .withLoadLimit(loadLimit)
                .withInterruptible(interruptible);
    }
}
