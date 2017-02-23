/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.exception.BaseException;

import java.io.IOException;

/**
 * Wrapper for java.util.IOException.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-22 (15:47)
 */
public class UnderlyingIOException extends BaseException {
    protected UnderlyingIOException(IOException cause) {
        super(PrivateMessageSeeds.UNDERLYING_IO_EXCEPTION, cause);
    }
}