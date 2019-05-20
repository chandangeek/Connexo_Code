package com.elster.jupiter.soap.whiteboard.cxf;
import java.time.Instant;

public interface EndPointOccurrence {
    Instant getStartTime();
    Instant getEndTime();
    String getRequest();
    String getStatus();
    EndPointConfiguration getEndPointConfiguration();
    String getApplicationName();
}
