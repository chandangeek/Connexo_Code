package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.LogBookEventNr;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class LogBookEventRequestStructure extends Data<LogBookEventRequestStructure> {

    public static final int PADDING_DATA_LENGTH = 7;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.POOLING_REQUEST;

    private DateTime dateTime;
    private LogBookEventNr logBookEventNr;
    private PaddingData paddingData;

    private RequestFactory requestFactory;

    public LogBookEventRequestStructure(RequestFactory requestFactory, int logBookEventNr) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
        this.logBookEventNr = new LogBookEventNr(logBookEventNr);
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                logBookEventNr.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public LogBookEventRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime.parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.logBookEventNr.parse(rawData, ptr);
        ptr += logBookEventNr.getLength();

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public LogBookEventNr getLogBookEventNr() {
        return logBookEventNr;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}