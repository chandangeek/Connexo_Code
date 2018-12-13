package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class NumberOfSentFrames extends AbstractParameter {

    NumberOfSentFrames(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int number = 0;

    public int getNumber() {
        return number;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.NumberOfSentFrames;
    }

    @Override
    protected void parse(byte[] data) {
        number = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}
