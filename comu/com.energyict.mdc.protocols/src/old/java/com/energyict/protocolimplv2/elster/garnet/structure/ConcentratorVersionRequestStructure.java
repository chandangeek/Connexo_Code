package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;

import java.time.Clock;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConcentratorVersionRequestStructure extends Data<ConcentratorVersionRequestStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_VERSION_REQUEST;

    private DateTime dateTime;
    private final RequestFactory requestFactory;

    public ConcentratorVersionRequestStructure(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
    }

    @Override
    public byte[] getBytes() {
        return dateTime.getBytes();
    }

    @Override
    public ConcentratorVersionRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone()).parse(rawData, offset);
        return this;
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }
}