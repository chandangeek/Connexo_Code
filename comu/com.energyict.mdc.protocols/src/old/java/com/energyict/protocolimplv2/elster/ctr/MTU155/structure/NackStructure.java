package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NackAdditionalData;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NackReason;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class NackStructure extends Data<NackStructure> {

    private NackReason reason;
    private FunctionCode functionCode;
    private NackAdditionalData additionalData;

    public NackStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                reason.getBytes(),
                functionCode.getBytes(),
                additionalData.getBytes()
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public NackStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        this.reason = new NackReason().parse(rawData, ptr);
        ptr += reason.getLength();

        this.functionCode = new FunctionCode().parse(rawData, ptr);
        ptr += functionCode.getLength();

        this.additionalData = new NackAdditionalData().parse(rawData, ptr);
        ptr += additionalData.getLength();

        return this;
    }

    public NackAdditionalData getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(NackAdditionalData additionalData) {
        this.additionalData = additionalData;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }

    public NackReason getReason() {
        return reason;
    }

    public void setReason(NackReason reason) {
        this.reason = reason;
    }
}
