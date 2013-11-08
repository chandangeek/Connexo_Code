package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 13:59:45
 */
public class ReadOverSpeedParameters extends AbstractRadioCommand {

    public ReadOverSpeedParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int speedThreshold;             //Pulses per second
    private int timeForOverSpeedAlarm;      //Seconds

    public int getSpeedThreshold() {
        return speedThreshold;
    }

    public int getTimeForOverSpeedAlarm() {
        return timeForOverSpeedAlarm;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        speedThreshold = ProtocolTools.getIntFromBytes(data, 0, 2);
        timeForOverSpeedAlarm = ProtocolTools.getIntFromBytes(data, 2, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadOverspeedParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}