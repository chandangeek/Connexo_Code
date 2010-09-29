package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

import java.util.Arrays;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:45:35
 */
public abstract class Data extends AbstractPacketField {

    public static final int LENGTH = 128;
    private final byte[] data = new byte[LENGTH];

    protected void setData(byte[] data) {
        clearData();
        System.arraycopy(data, 0, this.data, 0, (data.length < LENGTH) ? data.length : LENGTH);
    }

    private void clearData() {
        Arrays.fill(this.data, (byte) 0x00);
    }

    public byte[] getBytes() {
        return data;
    }

}
