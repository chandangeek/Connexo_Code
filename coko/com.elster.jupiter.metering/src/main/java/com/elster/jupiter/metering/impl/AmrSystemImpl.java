package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import javax.inject.Provider;
import javax.inject.Inject;

import java.util.List;

public class AmrSystemImpl implements AmrSystem {
	//persistent fields
	private int id;
	private String name;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final Provider<MeterImpl> meterFactory;
    private final Provider<EndDeviceImpl> endDeviceFactory;

    @Inject
	AmrSystemImpl(DataModel dataModel, MeteringService meteringService,Provider<MeterImpl> meterFactory, Provider<EndDeviceImpl> endDeviceFactory) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.meterFactory = meterFactory;
        this.endDeviceFactory = endDeviceFactory;
    }

	AmrSystemImpl init(int id , String name) {
		this.id = id;
		this.name = name;
        return this;
	}
	
	@Override
	public int getId() {	
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	
	void save() {
		dataModel.persist(this);
	}

	@Override 
	public Meter newMeter(String amrId) {
		return newMeter(amrId, null);
	}
	@Override
	public Meter newMeter(String amrId, String mRID) {
		return meterFactory.get().init(this, amrId, mRID);
	}

	@Override 
	public EndDevice newEndDevice(String amrId) {
		return newEndDevice(amrId, null);
	}
	@Override
	public EndDevice newEndDevice(String amrId, String mRID) {
		return endDeviceFactory.get().init(this, amrId, mRID);
	}
	
	@Override
	public Optional<Meter> findMeter(String amrId) {
		Condition condition = Operator.EQUAL.compare("amrSystemId", getId());
		condition = condition.and(Operator.EQUAL.compare("amrId",amrId));
		List<Meter> candidates = meteringService.getMeterQuery().select(condition);
		switch(candidates.size()) {
			case 0:
				return Optional.absent();
			case 1:
				return Optional.of(candidates.get(0));
			default:
				throw new IllegalStateException();
		}
	}

}
