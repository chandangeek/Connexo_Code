package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class FirmwareVersion extends AbstractRadioCommand {

    FirmwareVersion(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int modeOfTransmission;
    private int firmwareVersion;

    public final int getModeOfTransmission() {
        return modeOfTransmission;
    }

    public final int getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public String toString() {
        return "V" + WaveflowProtocolUtils.toHexString(getFirmwareVersion()) + ", Mode of transmission " + getModeOfTransmission();
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.FirmwareVersion;
    }

    @Override
    protected void parse(byte[] data) {
        modeOfTransmission = ProtocolTools.getIntFromBytes(data, 1, 2);
        firmwareVersion = ProtocolTools.getIntFromBytes(data, 3, 2);
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }
}