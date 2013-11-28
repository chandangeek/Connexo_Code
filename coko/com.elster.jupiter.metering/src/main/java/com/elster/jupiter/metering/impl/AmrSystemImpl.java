package com.elster.jupiter.metering.impl;

import java.util.List;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

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
	
	@SuppressWarnings("unused")
	private AmrSystemImpl() {	
	}
	
	AmrSystemImpl(int id , String name) {
		this.id = id;
		this.name = name;
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
		Bus.getOrmClient().getAmrSystemFactory().persist(this);
	}

	@Override 
	public Meter newMeter(String amrId) {
		return new MeterImpl(this, amrId, null);
	}
	@Override
	public Meter newMeter(String amrId, String mRID) {
		return new MeterImpl(this, amrId, mRID);
	}

	@Override
	public Optional<Meter> findMeter(String amrId) {
		Condition condition = Operator.EQUAL.compare("amrSystemId", getId());
		condition = condition.and(Operator.EQUAL.compare("amrId",amrId));
		List<Meter> candidates = Bus.getMeteringService().getMeterQuery().select(condition);
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
