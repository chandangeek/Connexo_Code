package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import javax.inject.Inject;

public class ElectricityDetailImpl extends UsagePointDetailImpl implements ElectricityDetail {

    private boolean grounded;
    private Quantity nominalServiceVoltage;
    private PhaseCode phaseCode;
    private Quantity ratedCurrent;

    // last two fields currently only on E
    // we may want to generalize them later
    private Quantity ratedPower;
    private Quantity estimatedLoad;


    @Inject
    ElectricityDetailImpl(Clock clock) {
        super(clock);
    }

    static ElectricityDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, interval);
    }

    ElectricityDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        this.phaseCode = PhaseCode.UNKNOWN;
        return this;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public Quantity getNominalServiceVoltage() {
        return nominalServiceVoltage;
    }

    public PhaseCode getPhaseCode() {
        return phaseCode;
    }

    public Quantity getRatedCurrent() {
        return ratedCurrent;
    }

    public Quantity getRatedPower() {
        return ratedPower;
    }

    public Quantity getEstimatedLoad() {
        return estimatedLoad;
    }

    public void setGrounded(boolean grounded) {
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
}
