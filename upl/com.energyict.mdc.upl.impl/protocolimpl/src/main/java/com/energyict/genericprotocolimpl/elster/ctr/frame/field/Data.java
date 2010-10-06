package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Data<T extends Data> extends AbstractField<T> {

    private byte[] data;
    public static final int LENGTH = 128;

    public Data() {
        data = new byte[LENGTH];
    }

    public byte[] getBytes() {
        return data;
    }

    public T parse(byte[] rawData, int offset) {
        data = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        return (T) this;
    }

    public int getLength() {
        return LENGTH;
    }

}
