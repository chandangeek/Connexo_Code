package com.elster.jupiter.soap.whiteboard.cxf;
import java.time.Instant;

public interface EndPointOccurrence {
    Instant getStartTime();
    Instant getEndTime();
    String getRequest();
    WebService getWebService();
    String getWebServiceEndPointName();
    String getStatus();
    EndPointConfiguration getEndPointConfiguration();
}
