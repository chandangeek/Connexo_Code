package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.BaseException;

import javax.jms.JMSException;

/**
 * Thrown when the underlying Jms layer throws an Exception.
 */
public class UnderlyingJmsException extends BaseException {

    public UnderlyingJmsException(JMSException cause) {
        super(ExceptionTypes.UNDERLYING_JMS_EXCEPTION, cause);
    }
}
