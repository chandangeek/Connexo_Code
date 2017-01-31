/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models a log entry created for a service call
 * Created by bvn on 3/1/16.
 */
@ProviderType
public interface ServiceCallLog {
    LogLevel getLogLevel();

    ServiceCall getServiceCall();

    Instant getTime();

    String getMessage();

    String getStackTrace();

    /**
     * Removes a log entry from the system
     */
    void delete();
}
