package com.elster.jupiter.servicecall;

import java.time.Instant;

/**
 * Models a log entry created for a service call
 * Created by bvn on 3/1/16.
 */
public interface ServiceCallLog {
    LogLevel getLogLevel();

    ServiceCall getServiceCall();

    Instant getTime();

    String getMessage();
}
