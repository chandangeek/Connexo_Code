package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class NumberOfLoggedRecords extends AbstractParameter {

	public NumberOfLoggedRecords(WaveFlow waveFlow) {
		super(waveFlow);
	}

    private int numberOfRecords = 0;

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    @Override
    protected ParameterId getParameterId() {
		return ParameterId.NrOfLoggedRecords;
	}
	
	@Override
    public void parse(byte[] data) {
		numberOfRecords = ProtocolTools.getUnsignedIntFromBytes(data);
	}

	@Override
    protected byte[] prepare() {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(getClass(), "prepare");
	}
}
