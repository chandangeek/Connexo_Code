package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.time.UtcInstant;

class AmrSystemImpl implements AmrSystem {
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
	
	void persist() {
		Bus.getOrmClient().getAmrSystemFactory().persist(this);
	}

	@Override
	public Meter newMeter(String mRid) {
		return new MeterImpl(mRid, this);
	}

}
