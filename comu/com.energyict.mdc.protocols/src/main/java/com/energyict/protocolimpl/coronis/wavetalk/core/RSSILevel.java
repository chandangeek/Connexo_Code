/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;

public class RSSILevel extends AbstractRadioCommand {

    private int rssiLevel;
    private static final double MAX = 0x20;

    protected RSSILevel(AbstractWaveTalk waveTalk) {
        super(waveTalk);
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