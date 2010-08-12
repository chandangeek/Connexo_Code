package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.elster.ctr.packets.PacketField;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:45:35
 */
public class Data implements PacketField {

    private static final int MAX_DATA_LENGTH = 128;
    private final byte[] data = new byte[MAX_DATA_LENGTH];

    public Data(byte[] data) {
        setData(data);
    }

    protected void setData(byte[] data) {
        clearData();
        System.arraycopy(data, 0, this.data, 0, (data.length < MAX_DATA_LENGTH) ? data.length : MAX_DATA_LENGTH);
    }

    public Data() {
        clearData();
    }

    private void clearData() {
        Arrays.fill(this.data, (byte) 0x00);
    }

    public byte[] getBytes() {
        return data;
    }
}
