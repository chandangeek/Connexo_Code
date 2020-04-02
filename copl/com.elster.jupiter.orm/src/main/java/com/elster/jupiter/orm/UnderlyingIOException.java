/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.PersistenceException;

import java.io.IOException;

/**
 * Wrapper for java.util.IOException.
 */
public class UnderlyingIOException extends PersistenceException {
    public UnderlyingIOException(IOException cause) {
        super(MessageSeeds.UNDERLYING_IO_EXCEPTION, cause);
    }
}