package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class PoolingRequestStructure extends Data<PoolingRequestStructure> {

    public static final int LENGTH = FunctionCode.POOLING_REQUEST.getDataLength();

    private DateTime dateTime;
    private RequestFactory requestFactory;

    public PoolingRequestStructure(RequestFactory requestFactory) {
        super(LENGTH);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getTimeZone());
    }

    @Override
    public byte[] getBytes() {
        return dateTime.getBytes();
    }

    @Override
    public PoolingRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        this.dateTime = new DateTime(requestFactory.getTimeZone()).parse(rawData, offset);
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