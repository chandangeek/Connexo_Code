/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.HasQuantityMultiplier;
import com.elster.jupiter.util.units.HasQuantityUnit;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;

public class ElectricityDetailImpl extends UsagePointDetailImpl implements ElectricityDetail {

    private YesNoAnswer grounded;
    @HasQuantityMultiplier(min = 0, max = 9, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.VOLT}, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity nominalServiceVoltage;
    private PhaseCode phaseCode;
    @HasQuantityMultiplier(min = -3, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.AMPERE}, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity ratedCurrent;
    @HasQuantityMultiplier(min = -3, max = 12, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.WATT, Unit.VOLT_AMPERE}, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity ratedPower;
    @HasQuantityMultiplier(min = -3, max = 9, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.AMPERE, Unit.VOLT_AMPERE}, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity estimatedLoad;
    private YesNoAnswer limiter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String loadLimiterType;
    @HasQuantityUnit(units =  {Unit.WATT, Unit.VOLT_AMPERE}, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity loadLimit;
    private YesNoAnswer interruptible;

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
    public YesNoAnswer isGrounded() {
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
    public YesNoAnswer isLimiter() {
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
    public YesNoAnswer isInterruptible() {
        return interruptible;
    }

    public void setGrounded(YesNoAnswer grounded) {
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

    public void setLimiter(YesNoAnswer limiter) {
        this.limiter = limiter;
    }

    public void setLoadLimiterType(String loadLimiterType) {
        this.loadLimiterType = loadLimiterType;
    }

    public void setLoadLimit(Quantity loadLimit) {
        this.loadLimit = loadLimit;
    }

    public void setInterruptible(YesNoAnswer interruptible) {
        this.interruptible = interruptible;
    }
}
