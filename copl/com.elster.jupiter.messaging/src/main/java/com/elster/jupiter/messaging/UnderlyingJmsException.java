package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

import javax.jms.JMSException;

/**
 * Thrown when the underlying Jms layer throws an Exception.
 */
public class UnderlyingJmsException extends BaseException {

    public UnderlyingJmsException(Thesaurus thesaurus, JMSException cause) {
        super(ExceptionTypes.UNDERLYING_JMS_EXCEPTION, buildMessage(thesaurus), cause);
    }

    private static String buildMessage(Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.UNDERLYING_JMS_EXCEPTION).format();
    }
}
