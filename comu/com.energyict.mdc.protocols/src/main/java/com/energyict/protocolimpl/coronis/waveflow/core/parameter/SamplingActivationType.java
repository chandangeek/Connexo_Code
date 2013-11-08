package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class SamplingActivationType extends AbstractParameter {
	
	public SamplingActivationType(WaveFlow waveFlow) {
		super(waveFlow);
	}
	

	/**
	 * start hour when the data logging has to start. (in periodic step mode)
	 */
	int startHour=0;
	


	final int getStartHour() {
		return startHour;
	}

	final void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	@Override
    protected ParameterId getParameterId() {
		return ParameterId.SamplingActivationStartHour;
	}
	
	@Override
    public void parse(byte[] data) throws IOException {
		startHour = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
    protected byte[] prepare() throws IOException {
		return new byte[]{(byte)getStartHour()} ;
	}
}
