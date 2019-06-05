package com.elster.jupiter.soap.whiteboard.cxf;

import java.time.Instant;
import java.util.Optional;

public interface WebServiceCallOccurrence {
    long getId();
    EndPointConfiguration getEndPointConfiguration();
    Instant getStartTime();

    Optional<Instant> getEndTime();
    Optional<String> getRequest();
    WebServiceCallOccurrenceStatus getStatus();
    Optional<String> getPayload();
    Optional<String> getApplicationName();

    void log(LogLevel logLevel, String message);
    void log(String message, Exception exception);

    void setEndTime(Instant endTime);
    void setRequest(String request);
    void setStatus(WebServiceCallOccurrenceStatus status);
    void setPayload(String payload);
    void setApplicationName(String applicationName);

    void save();
}
