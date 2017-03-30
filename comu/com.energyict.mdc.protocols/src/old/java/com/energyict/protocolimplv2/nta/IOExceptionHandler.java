/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta;

import com.energyict.mdc.common.ComServerExecutionException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.DlmsSession;

import java.io.IOException;

public class IOExceptionHandler {

    /**
     * Throw the proper ComServer runtime exception
     */
    public static ComServerExecutionException handle(IOException e, DlmsSession dlmsSession) {
        if (isUnexpectedResponse(e, dlmsSession)) {
            return new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        } else {
            return communicationException(e, dlmsSession);
        }
    }

    /**
     * Indicates if the exception occurred because the meter returned an unexpected response.
     * In case of communication problem, throw the proper ComServer runtime exception.
     */
    public static boolean isUnexpectedResponse(IOException e, DlmsSession dlmsSession) {
        if (e instanceof NestedIOException) {
            Throwable cause = e.getCause();
            if (cause instanceof ExceptionResponseException || cause instanceof ProtocolException || cause instanceof DataAccessResultException) {
                return true;
            } else {
                throw communicationException(e, dlmsSession);
            }
        } else if (e instanceof ExceptionResponseException || e instanceof ProtocolException || e instanceof DataAccessResultException) {
            return true;
        } else {
            throw communicationException(e, dlmsSession);
        }
    }

    /**
     * Indicates if the exception is a {@link DataAccessResultException} describing an object is not supported by the device
     */
    public static boolean isNotSupportedDataAccessResultException(IOException e) {
        if (e instanceof DataAccessResultException) {
            switch (((DataAccessResultException) e).getCode()) {
                case OBJECT_UNDEFINED:
                case OBJECT_UNAVAILABLE:
                    return true;
            }
        }
        return false;
    }

    private static ComServerExecutionException communicationException(IOException e, DlmsSession dlmsSession) {
        return new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, dlmsSession.getProperties().getRetries() + 1);
    }

}
