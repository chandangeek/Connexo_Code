/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for event registration requests for warning log messages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:44)
 */
class WarningLoggingRequestType extends LoggingRequestType {

    @Override
    protected String getLogLevelName () {
        return "warnings";
    }

    @Override
    protected LogLevel getLogLevel () {
        return LogLevel.WARN;
    }

}