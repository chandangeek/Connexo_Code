package com.energyict.protocolimpl.coronis.wavetalk.core;

abstract class AbstractApplicationStatus extends AbstractParameter {

	
	abstract int getStatus();
	abstract void setStatus(int status);
	
	AbstractApplicationStatus(WaveFlow waveFlow) {
		super(waveFlow);
	}

}
