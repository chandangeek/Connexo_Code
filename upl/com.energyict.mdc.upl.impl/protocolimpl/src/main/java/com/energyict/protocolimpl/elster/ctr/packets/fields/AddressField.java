package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:41:13
 */
public class AddressField implements PacketField {

    private final int address;

    public AddressField(int address) {
        this.address = address & 0x0FFFFFF;
    }

    public AddressField() {
        this(0x0FFFFFF);
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
        byte[] asBytes = new byte[3];
        asBytes[0] = (byte) ((this.address >> 16) & 0x0FF);
        asBytes[1] = (byte) ((this.address >> 8) & 0x0FF);
        asBytes[2] = (byte) ((this.address >> 0) & 0x0FF);
        return asBytes;
    }

    @Override
    public String toString() {
        return "AddressField = " + ProtocolTools.getHexStringFromBytes(getBytes());
    }

}
