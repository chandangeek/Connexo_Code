package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 13:59:45
 */
public class WriteOverSpeedParameters extends AbstractRadioCommand {

    public WriteOverSpeedParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int speedThreshold;             //Pulses per second
    private int timeForOverSpeedAlarm;      //Seconds

    public void setSpeedThreshold(int speedThreshold) {
        this.speedThreshold = speedThreshold;
    }

    public void setTimeForOverSpeedAlarm(int timeForOverSpeedAlarm) {
        this.timeForOverSpeedAlarm = timeForOverSpeedAlarm;
    }

    @Override
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the over speed parameters, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() {
        byte[] secondArray = ProtocolTools.getBytesFromInt(timeForOverSpeedAlarm, 2);
        byte[] firstArray = ProtocolTools.getBytesFromInt(speedThreshold, 2);
        return ProtocolTools.concatByteArrays(firstArray, secondArray);
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteOverspeedParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}