package com.energyict.protocolimplv2.elster.garnet.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 14:03
 */
public class Data<T extends  Data> extends AbstractField<T> {

    private final int length;
    private byte[] data;
    private FunctionCode functionCode;

    public Data() {
        this.length = 0;
        this.data = new byte[0];
    }

    public Data(FunctionCode functionCode) {
        this.functionCode = functionCode;
        this.length = functionCode.getDataLength();
        this.data = new byte[this.length];
    }

    @Override
    public byte[] getBytes() {
        return data;
    }

    @Override
    public T parse(byte[] rawData, int offset) throws ParsingException {
        this.data = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return (T) this;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }
}
