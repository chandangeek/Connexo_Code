package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class Channel extends AbstractField<Channel> {

    private int channel;

    public Channel() {
    }

    public Channel(int channel) {
        this.channel = channel;
    }

    public int getLength() {
        return 1;
    }
    
    public byte[] getBytes() {
        return getBytesFromInt(channel, getLength());
    }

    public Channel parse(byte[] rawData, int offset) {
        channel = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
    
}
