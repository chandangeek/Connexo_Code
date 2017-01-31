/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

import java.io.IOException;

/**
 * Wrapper for java.util.IOException.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-20 (14:27)
 */
public class UnderlyingIOException extends BaseException {
    public UnderlyingIOException(IOException cause) {
        super(MessageSeeds.UNDERLYING_IO_EXCEPTION, cause);
    }
}