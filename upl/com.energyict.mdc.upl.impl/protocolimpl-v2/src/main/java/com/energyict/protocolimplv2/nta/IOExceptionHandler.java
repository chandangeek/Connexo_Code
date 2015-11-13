package com.energyict.protocolimplv2.nta;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

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
    public static ProtocolRuntimeException handle(IOException e, DlmsSession dlmsSession) {
    	if (isUnexpectedResponse(e, dlmsSession)) {
            //Unexpected problem or response, but we can still communicate with the device
            return CommunicationException.unexpectedResponse(e);
        } else {
            //We can no longer communicate with the device
            return connectionCommunicationException(e, dlmsSession);
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
                throw connectionCommunicationException(e, dlmsSession);
            }
        } else if (e instanceof ExceptionResponseException || e instanceof ProtocolException || e instanceof DataAccessResultException) {
            return true;
        } else {
            throw connectionCommunicationException(e, dlmsSession);
        }
    }

    /**
     * Indicates if the exception is a {@link DataAccessResultException} describing an object is not supported by the device
     * or if it the object is not present in the object list.
     */
    public static boolean isNotSupportedDataAccessResultException(IOException e) {
        if (e instanceof DataAccessResultException) {
            switch (((DataAccessResultException) e).getCode()) {
                case OBJECT_UNDEFINED:
                case OBJECT_UNAVAILABLE:
                    return true;
            }
        } else if (e instanceof NotInObjectListException) {
            return true;
        }
        return false;
    }
    
    /**
     * Checks whether the returned error is an authorization error (basically an R/W denied).
     * 
     * @param 		e		The IO error.
     * 
     * @return		<code>true</code> if this concerns an authorization problem, <code>false</code> if not.
     */
    public static final boolean isAuthorizationProblem(final IOException e) {
    	Throwable t = e;
    	
    	while (t != null) {
	    	if (t instanceof DataAccessResultException) {
	    		final DataAccessResultException dataAccessResultException = (DataAccessResultException)t;
	    		
	    		if (dataAccessResultException.getCode() == DataAccessResultCode.RW_DENIED) {
	    			return true;
	    		}
	    	}
	    	
	    	t = t.getCause();
    	}
    	
    	return false;
    }

    private static ProtocolRuntimeException connectionCommunicationException(IOException e, DlmsSession dlmsSession) {
        return ConnectionCommunicationException.numberOfRetriesReached(e, dlmsSession.getProperties().getRetries() + 1);
    }
}
