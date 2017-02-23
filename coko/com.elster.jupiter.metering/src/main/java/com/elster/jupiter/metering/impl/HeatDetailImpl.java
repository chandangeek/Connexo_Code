/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.HasQuantityMultiplier;
import com.elster.jupiter.util.units.HasQuantityUnit;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import javax.inject.Inject;
import java.time.Clock;

public class HeatDetailImpl extends UsagePointDetailImpl implements HeatDetail {

    @HasQuantityMultiplier(min = 0, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_MULTIPLIER + "}")
    @HasQuantityUnit(units =  {Unit.PASCAL}, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity pressure;
    @HasQuantityUnit(units =  {Unit.WATT_HOUR}, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.INVALID_UNIT + "}")
    private Quantity physicalCapacity;
    private YesNoAnswer bypass;
    private BypassStatus bypassStatus;
    private YesNoAnswer valve;

    @Inject
    HeatDetailImpl(Clock clock, DataModel dataModel) {
        super(clock, dataModel);
    }

    static HeatDetailImpl from(DataModel dataModel, UsagePoint usagePoint, Interval interval) {
        return dataModel.getInstance(HeatDetailImpl.class).init(usagePoint, interval);
    }

    HeatDetailImpl init(UsagePoint usagePoint, Interval interval) {
        super.init(usagePoint, interval);
        return this;
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
}
