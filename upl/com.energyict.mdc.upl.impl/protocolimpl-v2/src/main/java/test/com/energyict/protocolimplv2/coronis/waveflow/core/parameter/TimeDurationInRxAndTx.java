package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class TimeDurationInRxAndTx extends AbstractParameter {

    TimeDurationInRxAndTx(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int timeRx = 0;
    private int timeTx = 0;

    public int getTimeRx() {
        return timeRx;
    }

    public int getTimeTx() {
        return timeTx;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.TimeDurationRxAndTx;
    }

    @Override
    protected void parse(byte[] data) {
        byte[] rxBytes = ProtocolTools.getSubArray(data, 0, 2);
        rxBytes  = ProtocolTools.reverseByteArray(rxBytes);
        timeRx = ProtocolTools.getUnsignedIntFromBytes(rxBytes);

        byte[] txBytes = ProtocolTools.getSubArray(data, 2, 4);
        txBytes = ProtocolTools.reverseByteArray(txBytes);
        timeTx = ProtocolTools.getUnsignedIntFromBytes(txBytes);
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}
