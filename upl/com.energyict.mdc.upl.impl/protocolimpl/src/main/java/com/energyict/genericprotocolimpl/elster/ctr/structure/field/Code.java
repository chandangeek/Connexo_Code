package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Class for the StartDate field in a CTR Structure Object - used in firmware upgrade process
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class Code extends AbstractField<Code> {

    private byte[] code;

    public byte[] getBytes() {
        return code;
    }

    public byte[] getCode() {
        return code;
    }

    public int getLength() {
        return code.length;
    }

    public Code parse(byte[] rawData, int offset) throws CTRParsingException {
        // From offset point up to the end, rawData contains Code bytes
        code = ProtocolTools.getSubArray(rawData, offset, rawData.length);
        return this;
    }

    public void setCode(byte[] code) {
        this.code = code.clone();
    }
}