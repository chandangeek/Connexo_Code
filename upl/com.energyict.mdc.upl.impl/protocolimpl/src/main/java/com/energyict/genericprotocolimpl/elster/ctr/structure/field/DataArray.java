package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigInteger;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class DataArray extends AbstractField<DataArray> {

    private byte[] data;

    public byte[] getBytes() {
        return data;
    }

    public DataArray parse(byte[] rawData, int offset) throws CTRParsingException {
        byte[] b = ProtocolTools.getSubArray(rawData, offset);
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
