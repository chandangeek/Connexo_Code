package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.SetMultimap;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface WebServiceCallOccurrence {
    /**
     * Used as a key for custom property of {@link org.apache.cxf.message.Message} containing id of {@link WebServiceCallOccurrence}.
     */
    String MESSAGE_CONTEXT_OCCURRENCE_ID = "com.honeywell.web.service.call.occurrence.id";

    long getId();

    Optional<String> getAppServerName();

    EndPointConfiguration getEndPointConfiguration();
    Instant getStartTime();

    Optional<Instant> getEndTime();
    Optional<String> getRequest();
    WebServiceCallOccurrenceStatus getStatus();
    Optional<String> getPayload();
    Optional<String> getApplicationName();

    void log(LogLevel logLevel, String message);
    void log(String message, Exception exception);

    Finder<EndPointLog> getLogs();

    void setEndTime(Instant endTime);
    void setRequest(String request);
    void setStatus(WebServiceCallOccurrenceStatus status);
    void setPayload(String payload);
    void setApplicationName(String applicationName);
    void saveRelatedAttribute(String key, String value);
    void saveRelatedAttributes(SetMultimap<String, String> values);
    void retry();

    void save();
}
