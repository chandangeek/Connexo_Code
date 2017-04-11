package com.energyict.dlms.exceptionhandler;

import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

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
public class DLMSIOExceptionHandler {

    /**
     * Throw the proper ComServer runtime exception
     */

    public static CommunicationException handle(IOException e, int nbRetries) {
    	if (isUnexpectedResponse(e, nbRetries)) {
            //Unexpected problem or response, but we can still communicate with the device
            return CommunicationException.unexpectedResponse(e);
        } else {
            //We can no longer communicate with the device
            return connectionCommunicationException(e, nbRetries);
        }
    }

    /**
     * Indicates if the exception occurred because the meter returned an unexpected response.
     * In case of communication problem, throw the proper ComServer runtime exception.
     */
    public static boolean isUnexpectedResponse(IOException e, int nbRetries) {
        if (e instanceof NestedIOException) {
            Throwable cause = e.getCause();
            if (cause instanceof ExceptionResponseException || cause instanceof ProtocolException || cause instanceof DataAccessResultException) {
                return true;
            } else {
                throw connectionCommunicationException(e, nbRetries);
            }
        } else if (e instanceof ExceptionResponseException || e instanceof ProtocolException || e instanceof DataAccessResultException) {
            return true;
        } else {
            throw connectionCommunicationException(e, nbRetries);
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
     * Indicates whether or not the given {@link IOException} is a {@link DataAccessResultCode#TEMPORARY_FAILURE}.
     * 
     * @param 		e		The {@link IOException}.
     * 
     * @return		<code>true</code> if the {@link IOException} is in fact a {@link DataAccessResultCode#TEMPORARY_FAILURE}, <code>false</code> otherwise.
     */
    public static final boolean isTemporaryFailure(final IOException e) {
    	if (e instanceof DataAccessResultException) {
    		return ((DataAccessResultException)e).getCode() == DataAccessResultCode.TEMPORARY_FAILURE;
    	}
    	
    	return false;
    }
    
    /**
     * Indicates whether or not the given {@link IOException} is a {@link DataAccessResultCode#TEMPORARY_FAILURE}.
     * 
     * @param 		e		The {@link IOException}.
     * 
     * @return		<code>true</code> if the {@link IOException} is in fact a {@link DataAccessResultCode#TEMPORARY_FAILURE}, <code>false</code> otherwise.
     */
    public static final boolean isTemporaryFailure(final IOException e) {
    	if (e instanceof DataAccessResultException) {
    		return ((DataAccessResultException)e).getCode() == DataAccessResultCode.TEMPORARY_FAILURE;
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

    private static CommunicationException connectionCommunicationException(IOException e, int noRetries) {
        return ConnectionCommunicationException.numberOfRetriesReached(e, noRetries);
    }
}
