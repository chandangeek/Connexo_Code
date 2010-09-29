package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:43:23
 */
public class StructureCode extends AbstractPacketField {

    public static final int LENGTH = 1;

    private int value;

    public StructureCode(int value) {
        this.value = value & 0x0FF;
    }

    public StructureCode() {
        this.value = 0x00;
    }

    public StructureCode(byte[] rawPacket, int offset) {
        this(rawPacket[offset] & 0x0FF);
    }

    public byte[] getBytes() {
        return new byte[] {(byte) (value & 0x0FF)};
    }
}
