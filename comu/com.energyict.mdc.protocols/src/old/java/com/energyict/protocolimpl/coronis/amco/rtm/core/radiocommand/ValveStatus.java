/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class ValveStatus extends AbstractRadioCommand {

    protected ValveStatus(RTM rtm) {
        super(rtm);
    }

    private int state;

    public int getState() {
        return state;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        state = data[0] & 0xFF;
    }

    public boolean isOpened() {
        return state == 0x00;
    }

    public boolean isClosed() {
        return state == 0x01;
    }

    public String getDescription() {
        return isOpened() ? "Open" : "Closed";
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ValveStatus;
    }
}
