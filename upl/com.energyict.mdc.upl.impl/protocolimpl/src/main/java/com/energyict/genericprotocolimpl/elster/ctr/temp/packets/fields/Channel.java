package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:44:02
 */
public class Channel extends AbstractPacketField {

    public static final int LENGTH = 1;

    private final int channel;

    public Channel(int channel) {
        this.channel = channel & 0x0F;
    }

    public Channel() {
        this(0);
    }

    public Channel(byte[] rawPacket, int offset) {
        this(rawPacket[offset] & 0x0FF);
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
    
}
