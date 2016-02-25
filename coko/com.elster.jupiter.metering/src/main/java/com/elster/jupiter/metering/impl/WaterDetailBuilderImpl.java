package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

public class WaterDetailBuilderImpl implements WaterDetailBuilder{

	private Optional<Boolean> collar = Optional.empty();
	private Boolean grounded;
	private Quantity pressure;
	private Quantity physicalCapacity;
	private Boolean limiter;
	private String loadLimiterType;
	private Quantity loadLimit;
	private Optional<Boolean> bypass = Optional.empty();
	private BypassStatus bypassStatus;
	private Optional<Boolean> valve = Optional.empty();
	private Optional<Boolean> capped = Optional.empty();
	private Optional<Boolean> clamped = Optional.empty();

	private UsagePoint usagePoint;
	private Interval interval;
	private DataModel dataModel;

	
	public WaterDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
		this.dataModel = dataModel;
		this.usagePoint = usagePointImpl;
		this.interval = interval;
	}

	@Override
	public WaterDetailBuilder withCollar(Boolean collar) {
		this.collar = Optional.ofNullable(collar);
		return this;
	}

	@Override
	public WaterDetailBuilder withGrounded(Boolean grounded) {
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
	public WaterDetailBuilder withLimiter(Boolean limiter) {
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
	public WaterDetailBuilder withBypass(Boolean bypass) {
		this.bypass = Optional.ofNullable(bypass);
		return this;
	}

	@Override
	public WaterDetailBuilder withBypassStatus(BypassStatus bypassStatus) {
		this.bypassStatus = bypassStatus;
		return this;
	}

	@Override
	public WaterDetailBuilder withValve(Boolean valve) {
		this.valve = Optional.ofNullable(valve);
		return this;
	}

	@Override
	public WaterDetailBuilder withCapped(Boolean capped) {
		this.capped = Optional.ofNullable(capped);
		return this;
	}

	@Override
	public WaterDetailBuilder withClamped(Boolean clamped) {
		this.clamped = Optional.ofNullable(clamped);
		return this;
	}

	@Override
	public Optional<Boolean> getCollar() {
		return collar;
	}

	@Override
	public Optional<Boolean> getClamped() {
		return clamped;
	}

	@Override
	public Boolean isGrounded() {
		return grounded;
	}

	@Override
	public Quantity getPressure() {
		return pressure;
	}

	@Override
	public Quantity getPhysicalCapacity() {
		return physicalCapacity;
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
	public Optional<Boolean> getBypass() {
		return bypass;
	}

	@Override
	public BypassStatus getBypassStatus() {
		return bypassStatus;
	}

	@Override
	public Optional<Boolean> getValve() {
		return valve;
	}

	@Override
	public Optional<Boolean> getCapped() {
		return capped;
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
		WaterDetailImpl wd = dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, interval);
		wd.setCollar(this.getCollar());
		wd.setGrounded(isGrounded());
		wd.setPressure(getPressure());
		wd.setPhysicalCapacity(getPhysicalCapacity());
		wd.setLimiter(isLimiter());
		wd.setLoadLimiterType(getLoadLimiterType());
		wd.setLoadLimit(getLoadLimit());
		wd.setBypass(getBypass());
		wd.setBypassStatus(getBypassStatus());
		wd.setValve(getValve());
		wd.setCapped(getCapped());
		wd.setClamped(getClamped());
		return wd;
	}
}
