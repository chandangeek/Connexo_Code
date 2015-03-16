package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;

public class EndDeviceImpl extends AbstractEndDeviceImpl<EndDeviceImpl> implements EndDevice {

    @Inject
	EndDeviceImpl(DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory) {
		super(dataModel, eventService, deviceEventFactory, EndDeviceImpl.class);
	}

}