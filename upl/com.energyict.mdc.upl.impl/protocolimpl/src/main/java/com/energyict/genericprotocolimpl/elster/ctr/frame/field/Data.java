package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Data extends AbstractField<Data> {

    private byte[] data;
    public static final int LENGTH = 128;

    public byte[] getBytes() {
        return data;
    }

    public Data parse(byte[] rawData, int offset) {
        data = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

}
