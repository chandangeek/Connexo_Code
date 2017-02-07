/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ElectricityUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {
    public YesNoAnswer grounded;
    public Quantity nominalServiceVoltage;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public Quantity estimatedLoad;
    public YesNoAnswer limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public YesNoAnswer interruptible;

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
    public ServiceKind getKind() {
        return ServiceKind.ELECTRICITY;
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
