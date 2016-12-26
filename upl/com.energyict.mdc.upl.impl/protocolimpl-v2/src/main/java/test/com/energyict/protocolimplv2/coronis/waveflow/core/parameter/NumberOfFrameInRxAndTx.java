package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocol.exceptions.CodingException;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class NumberOfFrameInRxAndTx extends AbstractParameter {

    NumberOfFrameInRxAndTx(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int numberOfFrameRx = 0;
    private int numberOfFrameTx = 0;

    public int getNumberOfFrameTx() {
        return numberOfFrameTx;
    }

    public int getNumberOfFrameRx() {
        return numberOfFrameRx;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.NumberOfFrameRxAndTx;
    }

    @Override
    protected void parse(byte[] data) {
        numberOfFrameRx = data[0] & 0xFF;
        numberOfFrameTx = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}
