package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;
import oracle.AQ.AQException;

/**
 * Thrown when the underlying Advanced Queuing layer thraws an AQException.
 */
public class UnderlyingAqException extends BaseException {

    public UnderlyingAqException(AQException cause) {
        super(ExceptionTypes.UNDERLYING_AQ_EXCEPTION, cause);
    }
}
