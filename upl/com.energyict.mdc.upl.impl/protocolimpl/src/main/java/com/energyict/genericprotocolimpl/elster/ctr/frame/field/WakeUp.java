package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class WakeUp extends AbstractField<WakeUp> {

    private byte[] data;
    public static final int LENGTH = 20;

    public byte[] getBytes() {
        return data;
    }

    public WakeUp parse(byte[] rawData, int offset) {
        data = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

}
