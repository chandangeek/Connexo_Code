/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Abstract super class for all Persistence related Exceptions
 */
public abstract class PersistenceException extends BaseException {
	
	private static final long serialVersionUID = 1;

    protected PersistenceException(MessageSeed messageSeed) {
        super(messageSeed);
    }

    protected PersistenceException(MessageSeed messageSeed, Object... args) {
        super(messageSeed, args);
    }

    protected PersistenceException(MessageSeed messageSeed, Throwable cause) {
        super(messageSeed, cause);
    }

    protected PersistenceException(MessageSeed messageSeed, Throwable cause, Object... args) {
        super(messageSeed, cause, args);
    }
}
