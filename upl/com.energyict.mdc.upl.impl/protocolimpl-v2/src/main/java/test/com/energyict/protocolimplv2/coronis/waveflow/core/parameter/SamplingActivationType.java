package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

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
    public void parse(byte[] data) {
		startHour = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
    protected byte[] prepare() {
		return new byte[]{(byte)getStartHour()} ;
	}
}
