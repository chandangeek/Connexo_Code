package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:42:42
 */
public class Aleo implements PacketField {

    private final int value;

    public Aleo() {
        this(new Random().nextInt() >> 8);
    }

    public Aleo(int value) {
        this.value = value & 0x0FF;
    }

    public int getValue() {
        return value;
    }

    public byte[] getBytes() {
        return new byte[]{(byte) (value & 0x0FF)};
    }

    @Override
    public String toString() {
        return "Channel = " + value;
    }

}
