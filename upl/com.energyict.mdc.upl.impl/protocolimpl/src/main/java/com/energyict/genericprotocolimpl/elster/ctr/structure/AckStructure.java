package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.FunctionCode;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.AckAdditionalData;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.AckCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:00:48
 */
public class AckStructure extends Data<AckStructure> {

    private AckCode ackCode;
    private FunctionCode functionCode;
    private AckAdditionalData additionalData;

    public AckStructure() {
        super(false);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                ackCode.getBytes(),
                functionCode.getBytes(),
                additionalData.getBytes()
        ));
    }

    @Override
    public AckStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        this.ackCode = new AckCode().parse(rawData, ptr);
        ptr += ackCode.getLength();

        this.functionCode = new FunctionCode().parse(rawData, ptr);
        ptr += functionCode.getLength();

        this.additionalData = new AckAdditionalData().parse(rawData, ptr);
        ptr += additionalData.getLength();

        return this;
    }

    public AckCode getAckCode() {
        return ackCode;
    }

    public void setAckCode(AckCode ackCode) {
        this.ackCode = ackCode;
    }

    public AckAdditionalData getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(AckAdditionalData additionalData) {
        this.additionalData = additionalData;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }
}
