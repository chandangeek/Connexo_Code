package com.energyict.protocolimpl.elster.ctr.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:41:13
 */
public class AddressField extends AbstractPacketField {

    public static final int LENGTH = 3;

    private final int address;

    public AddressField(int address) {
        this.address = address & 0x0FFFFFF;
    }

    public AddressField() {
        this(0x0FFFFFF);
    }

    public AddressField(byte[] rawPacket, int offset) {
        int addr = (rawPacket[offset] << 16) & 0x00FF0000;
        addr += (rawPacket[offset + 1] << 8) & 0x0000FF00;
        addr += rawPacket[offset + 2] & 0x000000FF;
        this.address = addr;
    }

    public int getAddress() {
        return address;
    }

    public boolean isBroadcastAddress() {
        return address == 0x0FFFFFF;
    }

    public boolean isInvalidAddress() {
        return address == 0;
    }

    public byte[] getBytes() {
        byte[] asBytes = new byte[LENGTH];
        asBytes[0] = (byte) ((this.address >> 16) & 0x0FF);
        asBytes[1] = (byte) ((this.address >> 8) & 0x0FF);
        asBytes[2] = (byte) ((this.address >> 0) & 0x0FF);
        return asBytes;
    }

}
