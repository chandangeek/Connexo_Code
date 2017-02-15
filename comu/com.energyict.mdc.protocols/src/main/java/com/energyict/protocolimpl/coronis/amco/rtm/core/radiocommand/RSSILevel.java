/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class RSSILevel extends AbstractRadioCommand {

    private int rssiLevel;
    private static final double MAX = 0x20;

    protected RSSILevel(RTM rtm) {
        super(rtm);
    }

    public RSSILevel(RTM rtm, int qos) {
        super(rtm);
        this.rssiLevel = qos;
    }

    /*
    A percentage representing the saturation. 100% = full saturation = 48 dB
    */
    public double getRssiLevel() {
        return (((double) rssiLevel) / MAX) * 100;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
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