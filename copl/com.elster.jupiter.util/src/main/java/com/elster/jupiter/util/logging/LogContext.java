/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.logging;

import aQute.bnd.annotation.ProviderType;

import java.util.logging.Level;

@ProviderType
public interface LogContext {

    void log(Level level, Object logger, String message, Throwable throwable, Object... args);

    void severe(Object logger, String message, Throwable throwable, Object... args);

    void severe(Object logger, Throwable throwable);

    void severe(Object logger, String message, Object... args);

    void warning(Object logger, String message, Object... args);

    void info(Object logger, String message, Object... args);

    void fine(Object logger, String message, Object... args);

    void finer(Object logger, String message, Object... args);

    void finest(Object logger, String message, Object... args);

}