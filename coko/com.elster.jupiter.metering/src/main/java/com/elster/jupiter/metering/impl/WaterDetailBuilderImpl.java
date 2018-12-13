/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Iterator;

public class WaterDetailBuilderImpl implements WaterDetailBuilder{

	private YesNoAnswer collar = YesNoAnswer.UNKNOWN;
	private YesNoAnswer grounded;
	private Quantity pressure;
	private Quantity physicalCapacity;
	private YesNoAnswer limiter;
	private String loadLimiterType;
	private Quantity loadLimit;
	private YesNoAnswer bypass = YesNoAnswer.UNKNOWN;
	private BypassStatus bypassStatus;
	private YesNoAnswer valve = YesNoAnswer.UNKNOWN;
	private YesNoAnswer capped = YesNoAnswer.UNKNOWN;
	private YesNoAnswer clamped = YesNoAnswer.UNKNOWN;

	private UsagePoint usagePoint;
	private Interval interval;
	private DataModel dataModel;

	
	public WaterDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
		this.dataModel = dataModel;
		this.usagePoint = usagePointImpl;
		this.interval = interval;
	}

	@Override
	public WaterDetailBuilder withCollar(YesNoAnswer collar) {
		this.collar = collar;
		return this;
	}

	@Override
	public WaterDetailBuilder withGrounded(YesNoAnswer grounded) {
		this.grounded = grounded;
		return this;
	}

	@Override
	public WaterDetailBuilder withPressure(Quantity pressure) {
		this.pressure = pressure;
		return this;
	}

	@Override
	public WaterDetailBuilder withPhysicalCapacity(Quantity physicalCapacity) {
		this.physicalCapacity = physicalCapacity;
		return this;
	}

	@Override
	public WaterDetailBuilder withLimiter(YesNoAnswer limiter) {
		this.limiter = limiter;
		return this;
	}

	@Override
	public WaterDetailBuilder withLoadLimit(Quantity loadLimit) {
		this.loadLimit = loadLimit;
		return this;
	}

	@Override
	public WaterDetailBuilder withLoadLimiterType(String loadLimiterType) {
		this.loadLimiterType = loadLimiterType;
		return this;
	}

	@Override
	public WaterDetailBuilder withBypass(YesNoAnswer bypass) {
		this.bypass = bypass;
		return this;
	}

	@Override
	public WaterDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
		this.bypassStatus = bypassStatus;
		return this;
	}

	@Override
	public WaterDetailBuilder withValve(YesNoAnswer valve) {
		this.valve = valve;
		return this;
	}

	@Override
	public WaterDetailBuilder withCap(YesNoAnswer capped) {
		this.capped = capped;
		return this;
	}

	@Override
	public WaterDetailBuilder withClamp(YesNoAnswer clamped) {
		this.clamped = clamped;
		return this;
	}


	@Override
	public WaterDetail create() {
		WaterDetail detail= buildDetail();
		usagePoint.addDetail(detail);
        return detail;
    }

	@Override
	public void validate() {
		Save.CREATE.validate(dataModel,buildDetail());
	}

	private WaterDetail buildDetail(){
        Range<Instant> newDetailRange = interval.toClosedOpenRange();
        Iterator<? extends UsagePointDetail> iterator = usagePoint.getDetail(newDetailRange).iterator();
        if (iterator.hasNext()) {
            usagePoint.terminateDetail(iterator.next(), newDetailRange.lowerEndpoint());
            if (iterator.hasNext()) {
                interval = Interval.of(Range.closedOpen(newDetailRange.lowerEndpoint(), iterator.next().getRange().lowerEndpoint()));
            }
        }
        WaterDetailImpl wd = dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, interval);
		wd.setCollar(collar);
		wd.setGrounded(grounded);
		wd.setPressure(pressure);
		wd.setPhysicalCapacity(physicalCapacity);
		wd.setLimiter(limiter);
		wd.setLoadLimiterType(loadLimiterType);
		wd.setLoadLimit(loadLimit);
		wd.setBypass(bypass);
		wd.setBypassStatus(bypassStatus);
		wd.setValve(valve);
		wd.setCap(capped);
		wd.setClamp(clamped);
		return wd;
	}
}
