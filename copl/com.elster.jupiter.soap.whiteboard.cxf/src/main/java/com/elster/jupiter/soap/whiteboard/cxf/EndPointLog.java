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

    Instant getTime();

    String getMessage();

    /**
     * Removes a log entry from the system
     */
    void delete();
}
