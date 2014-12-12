package com.energyict.protocolimplv2.elster.garnet.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class Function extends AbstractField<Function> {

    public static final int LENGTH = 1;

    private FunctionCode functionCode;

    public Function() {
        this.functionCode = FunctionCode.UNKNOWN;
    }

    public Function(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(functionCode.getFunctionCode(), LENGTH);
    }

    @Override
    public Function parse(byte[] rawData, int offset) throws ParsingException {
        int code = getIntFromBytesLE(rawData, offset, LENGTH);
        functionCode = FunctionCode.fromCode(code);
        if (functionCode.equals(FunctionCode.UNKNOWN)) {
            throw new ParsingException("Encountered invalid/unknown function code " + ProtocolTools.getHexStringFromInt(code, 1, "0x") + ".");
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }
}