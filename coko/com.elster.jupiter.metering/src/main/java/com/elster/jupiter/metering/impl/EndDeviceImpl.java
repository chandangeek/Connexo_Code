/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

class
EndDeviceImpl extends AbstractEndDeviceImpl<EndDeviceImpl> implements EndDevice {

    @Inject
	EndDeviceImpl(Clock clock, DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory) {
		super(clock, dataModel, eventService, deviceEventFactory, EndDeviceImpl.class);
	}

}