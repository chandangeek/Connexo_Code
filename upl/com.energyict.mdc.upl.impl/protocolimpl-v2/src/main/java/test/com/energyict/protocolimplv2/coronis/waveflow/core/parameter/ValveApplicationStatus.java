package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class ValveApplicationStatus extends AbstractParameter {

    int status;

    ValveApplicationStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getStatus() {
        return status;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ValveApplicationStatus;
    }

    @Override
    protected void parse(byte[] data) {
        status = ProtocolTools.getIntFromBytes(data, 0, 1);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) status};
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Resets a flag in the status byte.
     *
     * @param bit: zero based index of the bit that should be set to 0.
     */
    public void resetBit(int bit) {
        status = status & ~(0x01 << bit);
    }
}