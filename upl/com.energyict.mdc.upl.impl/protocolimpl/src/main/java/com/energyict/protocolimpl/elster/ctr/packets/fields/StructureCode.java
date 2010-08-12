package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:43:23
 */
public class StructureCode implements PacketField {

    private int value;

    public StructureCode(int value) {
        this.value = value & 0x0FF;
    }

    public StructureCode() {
        this.value = 0x00;
    }

    public byte[] getBytes() {
        return new byte[] {(byte) (value & 0x0FF)};
    }
}
