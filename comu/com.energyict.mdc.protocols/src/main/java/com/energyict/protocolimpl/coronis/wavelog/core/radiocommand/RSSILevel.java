package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 4-mei-2011
 * Time: 17:29:20
 */
public class RSSILevel extends AbstractRadioCommand {

    private int rssiLevel;
    private static final double MAX = 0x20;

    protected RSSILevel(WaveLog waveLog) {
        super(waveLog);
    }

    /*
    A percentage representing the saturation. 100% = full saturation = 48 dB
    */
    public double getRssiLevel() {
        return (((double) rssiLevel) / MAX) * 100;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        rssiLevel = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.RssiLevel;
    }
}