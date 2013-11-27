package com.elster.jupiter.metering.impl;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.readings.MeterReading;
import com.google.common.collect.ImmutableList;

public class MeterImpl extends AbstractEndDeviceImpl implements Meter {
	
	@SuppressWarnings("unused")
	private AmrSystem amrSystem;
	private List<MeterActivation> meterActivations;
	@SuppressWarnings("unused")
	private MeterActivation currentMeterActivation;
	
	@SuppressWarnings("unused")
	private MeterImpl() {
		super();
	}
	
	MeterImpl(AmrSystem system, String amrId, String mRID) {
		super(system,amrId,mRID);				
	}
	
	

	@Override
	public List<MeterActivation> getMeterActivations() {
		return ImmutableList.copyOf(doGetMeterActivations());
	}
	
	private  List<MeterActivation> doGetMeterActivations() {
		if (meterActivations == null) {
			meterActivations = Bus.getOrmClient().getMeterActivationFactory().find("meter",this);
		}
		return meterActivations;
	}
		
	@Override
	public void store(MeterReading meterReading) {
		new MeterReadingStorer(this, meterReading).store();
	}
	
	@Override
	public MeterActivation activate(Date start) {
		MeterActivation result = new MeterActivationImpl(this, start);
		Bus.getOrmClient().getMeterActivationFactory().persist(result);
		return result;
	}
}
