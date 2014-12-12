package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

/**
 * Class for the channel field in a frame
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
