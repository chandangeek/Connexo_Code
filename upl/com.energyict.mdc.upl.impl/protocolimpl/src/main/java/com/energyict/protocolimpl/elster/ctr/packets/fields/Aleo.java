package com.energyict.protocolimpl.elster.ctr.packets.fields;

import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:42:42
 */
public class Aleo extends AbstractPacketField {

    public static final int LENGTH = 1;

    private final int value;

    public Aleo() {
        this(new Random().nextInt() >> 8);
    }

    public Aleo(int value) {
        this.value = value & 0x0FF;
    }

    public Aleo(byte[] rawPacket, int offset) {
        this(rawPacket[offset] & 0x0FF);
    }

    public int getValue() {
        return value;
    }

    public byte[] getBytes() {
        return new byte[]{(byte) (value & 0x0FF)};
    }

}
