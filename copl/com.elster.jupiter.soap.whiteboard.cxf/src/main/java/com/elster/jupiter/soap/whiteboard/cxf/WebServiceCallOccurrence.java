package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallOccurrenceImpl;

import java.time.Instant;
import java.util.Optional;

public interface WebServiceCallOccurrence {
    /**
     * Used as a key for custom property of {@link org.apache.cxf.message.Message} containing id of {@link WebServiceCallOccurrence}.
     */
    String MESSAGE_CONTEXT_OCCURRENCE_ID = "com.honeywell.web.service.call.occurrence.id";

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
    void retry();

    void save();

    WebServiceCallOccurrenceImpl init(Instant startTime,
                                      String requestName,
                                      String applicationName,
                                      EndPointConfiguration endPointConfiguration,
                                      String payload);
    WebServiceCallOccurrenceImpl init(Instant startTime,
                                      String requestName,
                                      String applicationName,
                                      EndPointConfiguration endPointConfiguration);

}
