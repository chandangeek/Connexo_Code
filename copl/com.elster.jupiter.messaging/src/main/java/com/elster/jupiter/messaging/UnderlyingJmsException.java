/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import javax.jms.JMSException;

/**
 * Thrown when the underlying Jms layer throws an Exception.
 */
public class UnderlyingJmsException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public UnderlyingJmsException(Thesaurus thesaurus, JMSException cause) {
        super(thesaurus, MessageSeeds.UNDERLYING_JMS_EXCEPTION, cause);
    }
}
