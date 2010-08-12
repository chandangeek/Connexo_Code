package com.energyict.protocolimpl.elster.ctr.packets.fields;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:45:35
 */
public class Data extends AbstractPacketField {

    public static final int LENGTH = 128;

    private final byte[] data = new byte[LENGTH];

    public Data(byte[] data) {
        setData(data);
    }

    public Data(byte[] rawPacket, int offset) {
        System.arraycopy(rawPacket, offset, data, 0, LENGTH);
    }

    protected void setData(byte[] data) {
        clearData();
        System.arraycopy(data, 0, this.data, 0, (data.length < LENGTH) ? data.length : LENGTH);
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
