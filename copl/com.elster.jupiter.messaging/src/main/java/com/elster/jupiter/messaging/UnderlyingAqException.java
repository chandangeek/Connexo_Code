/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import oracle.AQ.AQException;

/**
 * Thrown when the underlying Advanced Queuing layer throws an AQException.
 */
public class UnderlyingAqException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public UnderlyingAqException(Thesaurus thesaurus, AQException cause) {
        super(thesaurus, MessageSeeds.UNDERLYING_AQ_EXCEPTION, cause);
    }
}
