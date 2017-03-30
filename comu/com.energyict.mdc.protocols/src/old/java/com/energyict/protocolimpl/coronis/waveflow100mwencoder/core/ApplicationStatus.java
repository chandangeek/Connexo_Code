/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

abstract class ApplicationStatus extends AbstractParameter {

	
	abstract int getStatus();
	abstract void setStatus(int status);
	
	ApplicationStatus(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
		// TODO Auto-generated constructor stub
	}

}
