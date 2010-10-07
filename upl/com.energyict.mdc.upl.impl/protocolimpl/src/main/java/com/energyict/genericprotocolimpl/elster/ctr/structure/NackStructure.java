package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.FunctionCode;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.AdditionalData;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.NackReason;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class NackStructure extends Data<NackStructure> {

    private NackReason reason;
    private FunctionCode functionCode;
    private AdditionalData additionalData;

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                reason.getBytes(),
                functionCode.getBytes(),
                additionalData.getBytes()
        ));
    }

    @Override
    public NackStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        this.reason = new NackReason().parse(rawData, ptr);
        ptr += NackReason.LENGTH;

        this.functionCode = new FunctionCode().parse(rawData, ptr);
        ptr += NackReason.LENGTH;

        this.additionalData = new AdditionalData().parse(rawData, ptr);
        ptr += AdditionalData.LENGTH;

        return this;
    }

    public NackReason getReason() {
        return reason;
    }

    public void setReason(NackReason reason) {
        this.reason = reason;
    }

}
