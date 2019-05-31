package com.elster.jupiter.soap.whiteboard.cxf;
import java.time.Instant;

public interface EndPointOccurrence {
    long getId();
    Instant getStartTime();
    Instant getEndTime();
    String getRequest();
    String getStatus();
    String getPayload();
    EndPointConfiguration getEndPointConfiguration();
    String getApplicationName();
    void log(LogLevel logLevel, String message);
    void log(String message, Exception exception);
    void setPayload(String payload);
    void setStatus(String status);
    void setStartTime(Instant startTime);
    void setEndTime(Instant endTime);
    void setRequest(String request);
    void save();
}
