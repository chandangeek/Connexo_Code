package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

public class MeterImpl extends AbstractEndDeviceImpl<MeterImpl> implements Meter {
	
	@SuppressWarnings("unused")
	private AmrSystem amrSystem;
	private List<MeterActivation> meterActivations;
	@SuppressWarnings("unused")
	private MeterActivation currentMeterActivation;
    private MeteringService meteringService;

    @SuppressWarnings("unused")
    @Inject
	MeterImpl(DataModel dataModel, EventService eventService) {
        super(dataModel, eventService, MeterImpl.class);
    }
	
	static MeterImpl from(DataModel dataModel, AmrSystem system, String amrId, String mRID) {
		 return dataModel.getInstance(MeterImpl.class).init(system, amrId, mRID);
	}

    @Override
	public List<MeterActivation> getMeterActivations() {
		return ImmutableList.copyOf(doGetMeterActivations());
	}

    private List<MeterActivation> doGetMeterActivations() {
        if (meterActivations == null) {
            meterActivations = getDataModel().mapper(MeterActivation.class).find("meter", this);
        }
        return meterActivations;
    }
		
	@Override
	public void store(MeterReading meterReading) {
		new MeterReadingStorer(getDataModel(), meteringService, this, meterReading).store();
	}
	
	@Override
	public MeterActivation activate(Date start) {
		MeterActivation result = MeterActivationImpl.from(getDataModel(), this, start);
        getDataModel().mapper(MeterActivation.class).persist(result);
		return result;
	}

}
