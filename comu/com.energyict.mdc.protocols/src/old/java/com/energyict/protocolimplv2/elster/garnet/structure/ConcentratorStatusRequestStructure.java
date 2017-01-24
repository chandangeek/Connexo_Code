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
public class ConcentratorStatusRequestStructure extends Data<ConcentratorStatusRequestStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_STATUS_REQUEST;

    private DateTime dateTime;
    private RequestFactory requestFactory;

    public ConcentratorStatusRequestStructure(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
    }

    @Override
    public byte[] getBytes() {
        return dateTime.getBytes();
    }

    @Override
    public ConcentratorStatusRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        this.dateTime = new DateTime(this.requestFactory.getClock(), requestFactory.getTimeZone()).parse(rawData, offset);
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