/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
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

public class WaterDetailImpl extends UsagePointDetailImpl implements WaterDetail {

    private YesNoAnswer grounded;
    @HasQuantityMultiplier(min = 0, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.PASCAL}, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity pressure;
    @HasQuantityUnit(units =  {Unit.CUBIC_METER_PER_HOUR}, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity physicalCapacity;
    private YesNoAnswer limiter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String loadLimiterType;
    @HasQuantityUnit(units =  {Unit.CUBIC_METER_PER_HOUR}, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity loadLimit;
    private YesNoAnswer bypass;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve;
    private YesNoAnswer capped;
    private YesNoAnswer clamped;

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

    @Override
    public YesNoAnswer isGrounded() {
        return grounded;
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
    public Quantity getPhysicalCapacity() {
        return physicalCapacity;
    }

    @Override
    public Quantity getPressure() {
        return pressure;
    }

    @Override
    public YesNoAnswer isBypassInstalled() {
        return bypass;
    }

    @Override
    public BypassStatus getBypassStatus() {
        return bypassStatus;
    }

    @Override
    public YesNoAnswer isValveInstalled() {
        return valve;
    }

    @Override
    public YesNoAnswer isCapped() {
        return capped;
    }

    @Override
    public YesNoAnswer isClamped() {
        return clamped;
    }

    public void setGrounded(YesNoAnswer grounded) {
        this.grounded = grounded;
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

    public void setPressure(Quantity pressure) {
        this.pressure = pressure;
    }

    public void setPhysicalCapacity(Quantity physicalCapacity) {
        this.physicalCapacity = physicalCapacity;
    }

    public void setBypass(YesNoAnswer bypass) {
        this.bypass = bypass;
    }

    public void setBypassStatus(BypassStatus bypassStatus) {
        this.bypassStatus = bypassStatus;
    }

    public void setValve(YesNoAnswer valve) {
        this.valve = valve;
    }

    public void setCap(YesNoAnswer capped) {
        this.capped = capped;
    }

    public void setClamp(YesNoAnswer clamped) {
        this.clamped = clamped;
    }
}
