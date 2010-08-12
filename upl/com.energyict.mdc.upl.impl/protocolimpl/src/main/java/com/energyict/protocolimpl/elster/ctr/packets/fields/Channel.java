package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:44:02
 */
public class Channel implements PacketField {

    private final int channel;

    public Channel(int channel) {
        this.channel = channel & 0x0F;
    }

    public Channel() {
        this(0);
    }

    public boolean isGenericChannel() {
        return channel == 0;
    }

    public boolean isBroadcastChannel() {
        return channel == 0x0F;
    }

    public byte[] getBytes() {
        return new byte[] {(byte) channel};
    }
    
    @Override
    public String toString() {
        return "Channel = " + channel;
    }

}
