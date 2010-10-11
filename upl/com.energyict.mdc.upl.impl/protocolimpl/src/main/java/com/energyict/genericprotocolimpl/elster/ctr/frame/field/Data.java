package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
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

    /**
     *
     * @param fieldData
     * @return
     */
    protected byte[] padData(byte[] fieldData) {
        int paddingLength = LENGTH - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, LENGTH);
        }
        return fieldData;
    }

    public byte[] getBytes() {
        return data;
    }

    public T parse(byte[] rawData, int offset) throws CTRParsingException {
        data = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        return (T) this;
    }

}
