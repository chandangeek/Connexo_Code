package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeterImpl extends AbstractEndDeviceImpl<MeterImpl> implements Meter {
	
	@SuppressWarnings("unused")
	private Reference<AmrSystem> amrSystem = ValueReference.absent();
	private List<MeterActivation> meterActivations = new ArrayList<>();
	
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;

    @Inject
	MeterImpl(DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory,
              MeteringService meteringService, Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory) {
        super(dataModel, eventService, deviceEventFactory, MeterImpl.class);
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.deviceEventFactory = deviceEventFactory;
    }

    @Override
	public List<MeterActivation> getMeterActivations() {
		return ImmutableList.copyOf(meterActivations);
	}
		
	@Override
	public void store(MeterReading meterReading) {
		new MeterReadingStorer(getDataModel(), meteringService, this, meterReading, thesaurus, getEventService(), deviceEventFactory).store();
	}
	
	@Override
	public MeterActivationImpl activate(Date start) {
		MeterActivationImpl result = meterActivationFactory.get().init(this, start);
        getDataModel().persist(result);
        meterActivations.add(result);
		return result;
	}

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        for (MeterActivation meterActivation : meterActivations) {
            if (meterActivation.isCurrent()) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.absent();
    }

}
