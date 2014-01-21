package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;
import oracle.AQ.AQException;

/**
 * Thrown when the underlying Advanced Queuing layer thraws an AQException.
 */
public class UnderlyingAqException extends BaseException {

    public UnderlyingAqException(Thesaurus thesaurus, AQException cause) {
        super(ExceptionTypes.UNDERLYING_AQ_EXCEPTION, buildMessage(thesaurus), cause);
    }

    private static String buildMessage(Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.UNDERLYING_AQ_EXCEPTION).format();
    }
}
