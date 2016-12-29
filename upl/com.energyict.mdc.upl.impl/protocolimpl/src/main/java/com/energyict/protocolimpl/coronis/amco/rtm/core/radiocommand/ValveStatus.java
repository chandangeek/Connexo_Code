package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 11:35:43
 */
public class ValveStatus extends AbstractRadioCommand {

    protected ValveStatus(RTM rtm) {
        super(propertySpecService, rtm);
    }

    private int state;

    public int getState() {
        return state;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
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
