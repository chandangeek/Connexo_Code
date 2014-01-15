package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class EndDeviceImpl extends AbstractEndDeviceImpl<EndDeviceImpl> implements EndDevice {
	
    @Inject
	EndDeviceImpl(DataModel dataModel, EventService eventService) {
		super(dataModel, eventService, EndDeviceImpl.class);
	}
	
	static EndDeviceImpl from(DataModel dataModel, AmrSystem system, String amrId, String mRID) {
		return dataModel.getInstance(EndDeviceImpl.class).init(system, amrId, mRID);
	}
	
}
