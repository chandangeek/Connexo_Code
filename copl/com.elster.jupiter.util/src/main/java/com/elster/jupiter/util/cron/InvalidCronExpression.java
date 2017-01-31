/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.cron;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when parsing of a cron String fails.
 */
public final class InvalidCronExpression extends BaseException {

	private static final long serialVersionUID = 1L;

	public InvalidCronExpression(String expression, Throwable cause) {
        super(MessageSeeds.INVALID_CRON_EXPRESSION, cause, expression);
    }

}
