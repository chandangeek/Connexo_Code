package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the data field in a frame
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Data<T extends Data> extends AbstractField<T> {

    private byte[] data;
    private boolean longFrame;

    public static final int LENGTH = 128;
    public static final int LENGTH_SHORTFRAME = 128;
    public static final int LENGTH_LONGFRAME = 1012;

    public Data(boolean longFrame) {
        this.data = new byte[longFrame ? LENGTH_LONGFRAME : LENGTH_SHORTFRAME];
        this.longFrame = longFrame;
    }

    /**
     *
     * @param fieldData
     * @return
     */
    protected byte[] padData(byte[] fieldData) {
        int paddingLength = LENGTH_SHORTFRAME - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, (longFrame ? LENGTH_LONGFRAME : LENGTH_SHORTFRAME));
        }
        return fieldData;
    }

    public byte[] getBytes() {
        return data;
    }

    public T parse(byte[] rawData, int offset) throws CTRParsingException {
        data = ProtocolTools.getSubArray(rawData, offset, offset + (longFrame ? LENGTH_LONGFRAME : LENGTH_SHORTFRAME));
        return (T) this;
    }

    public int getLength() {
        return data.length;
    }

}
