package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Function;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 26/05/2014 - 15:35
 */
public class NotExecutedErrorResponseStructure extends Data<NotExecutedErrorResponseStructure> {

    public static final int PADDING_DATA_LENGTH = 4;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.NOT_EXECUTED_RESPONSE;

    private Function function;
    private NotExecutedError notExecutedError;
    private PaddingData paddingData;

    private final TimeZone timeZone;

    public NotExecutedErrorResponseStructure(TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.timeZone = timeZone;
        this.function = new Function();
        this.notExecutedError = new NotExecutedError();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getFunction().getBytes(),
                getNotExecutedError().getBytes(),
                getPaddingData().getBytes()
        );
    }

    @Override
    public NotExecutedErrorResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.function = new Function().parse(rawData, ptr);
        ptr += function.getLength();

        this.notExecutedError = new NotExecutedError().parse(rawData, ptr);
        ptr += notExecutedError.getLength();

        this.paddingData = new PaddingData(PADDING_DATA_LENGTH).parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Function getFunction() {
        return function;
    }

    public NotExecutedError getNotExecutedError() {
        return notExecutedError;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}