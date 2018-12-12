/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models a log entry created for an end point
 * Created by bvn on 3/1/16.
 */
@ProviderType
public interface EndPointLog {
    LogLevel getLogLevel();

    EndPointConfiguration getEndPointConfiguration();

    /**
     * The timestamp of log creation
     */
    Instant getTime();

    /**
     * The message entered for this log.
     */
    String getMessage();

    /**
     * Retrieve the stacktrace, if one was logged. Null otherwise.
     */
    String getStackTrace();

    /**
     * Removes a log entry from the system
     */
    void delete();
}
