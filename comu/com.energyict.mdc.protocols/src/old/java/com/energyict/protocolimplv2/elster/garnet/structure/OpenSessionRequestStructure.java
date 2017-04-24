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
import com.energyict.protocolimplv2.elster.garnet.structure.field.SessionKeyPart;

/**
 * @author sva
 * @since 27/05/2014 - 10:29
 */
public class OpenSessionRequestStructure extends Data<OpenSessionRequestStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.OPEN_SESSION_REQUEST;

    private DateTime dateTime;
    private SessionKeyPart firstPartOfSessionKey;

    private RequestFactory requestFactory;

    public OpenSessionRequestStructure(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
        this.firstPartOfSessionKey = new SessionKeyPart();
        this.firstPartOfSessionKey.generateRandomHalfOfSessionKey();
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                firstPartOfSessionKey.getBytes()
        );
    }

    @Override
    public OpenSessionRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.firstPartOfSessionKey = new SessionKeyPart().parse(rawData, ptr);
        ptr += firstPartOfSessionKey.getLength();

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

    public SessionKeyPart getFirstPartOfSessionKey() {
        return firstPartOfSessionKey;
    }
}