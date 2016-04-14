package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import java.time.Clock;

/**
 * Created by bvn on 4/11/16.
 */
public class ElectricityTechnicalInfo extends UsagePointInfo {
    public Quantity nominalServiceVoltage;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public Quantity estimatedLoad;
    public YesNoAnswer limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public YesNoAnswer interruptible;

    @Override
    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Clock clock) {
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

    @Override
    ServiceKind getServiceKind() {
        return ServiceKind.ELECTRICITY;
    }
}
