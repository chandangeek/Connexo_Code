package com.energyict.protocolimplv2.nta;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;

/**
 * Handler for any kind of IOException (or one of its subclasses) received from the DLMS framework and its lower connection layers.
 * This can be used for the new V2 protocols.
 * <p/>
 * In general:
 * ExceptionResponseException, ProtocolException or DataAccessResultException mean that the meter has returned something unexpected (e.g. some kind of error code).
 * Other IOExceptions are related to problems with the communication (e.g. timeout, socket closed,... )
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 14:50
 * Author: khe
 */
public class IOExceptionHandler {

    /**
     * Throw the proper ComServer runtime exception
     */
    public static ComServerExecutionException handle(IOException e, DlmsSession dlmsSession) {
        if (isUnexpectedResponse(e, dlmsSession)) {
            return MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
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

    private static ComServerExecutionException communicationException(IOException e, DlmsSession dlmsSession) {
        return MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, dlmsSession.getProperties().getRetries() + 1);
    }
}
