/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
 * @since 23/05/2014 - 13:28
 */
public class ConfirmSuccessfulLogReceive extends Data<ConfirmSuccessfulLogReceive> {

    public static final int PADDING_DATA_LENGTH = 7;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONFIRM_LOG_RECEIVE;

    private DateTime dateTime;
    private LogBookEventNr logBookEventNr;
    private PaddingData paddingData;
    private RequestFactory requestFactory;

    public ConfirmSuccessfulLogReceive(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
        this.logBookEventNr = new LogBookEventNr();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    public ConfirmSuccessfulLogReceive(RequestFactory requestFactory, int logBookNr) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
        this.logBookEventNr = new LogBookEventNr(logBookNr);
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
    public ConfirmSuccessfulLogReceive parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime.parse(rawData, ptr);
        ptr+= dateTime.getLength();

        this.logBookEventNr.parse(rawData, ptr);
        ptr+= logBookEventNr.getLength();

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
}