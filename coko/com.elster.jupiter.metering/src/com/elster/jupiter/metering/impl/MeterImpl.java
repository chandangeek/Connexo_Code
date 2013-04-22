package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.time.UtcInstant;

public class MeterImpl implements Meter {
	// persistent fields
	private long id;
	private int amrSystemId;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private String serialNumber;
	private String utcNumber;
	private ElectronicAddress electronicAddress;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private AmrSystem amrSystem;
	
	@SuppressWarnings("unused")
	private MeterImpl() {
		this.electronicAddress = new ElectronicAddress();
	}
	
	MeterImpl(String mRID , AmrSystem system) {
		this.mRID = mRID;
		this.amrSystemId = system.getId();
		this.amrSystem = system;				
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getAliasName() {
		return aliasName == null ? "" : aliasName;
	}

	@Override
	public String getDescription() {
		return description == null ? "" : description;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name ==  null ? "" : name;
	}

	
	@Override
	public void save() {
		if (id == 0) {
			Bus.getOrmClient().getMeterFactory().persist(this);
		} else { 
			Bus.getOrmClient().getMeterFactory().update(this);
		}
	}

	@Override
	public String getSerialNumber() {
		return serialNumber == null ? "" : serialNumber;
	}

	@Override
	public String getUtcNumber() {
		return utcNumber == null ? "" : utcNumber;
	}

	@Override
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null ? null : electronicAddress.copy();
	}

	@Override
	public AmrSystem getAmrSystem() {
		if (amrSystem == null) {
			amrSystem = Bus.getOrmClient().getAmrSystemFactory().get(amrSystemId);
		}
		return amrSystem;
	}
	
}
