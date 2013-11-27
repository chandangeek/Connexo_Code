package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;

public class EndDeviceImpl extends AbstractEndDeviceImpl implements EndDevice {
	
	@SuppressWarnings("unused")
	private EndDeviceImpl() {
		super();
	}
	
	EndDeviceImpl(AmrSystem system, String amrId, String mRID) {
		super(system,amrId,mRID);				
	}
	
}
