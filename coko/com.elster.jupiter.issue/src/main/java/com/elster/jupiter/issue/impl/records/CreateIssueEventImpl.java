package com.elster.jupiter.issue.impl.records;

//TODO delete this class when events will be sent by MDC

/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
public class CreateIssueEventImpl {
    private static final long DEFAULT_TIMESTAMP = 1391260995932L;
    private Long timestamp;
    private String topic;
    private String deviceIdentifier;
    private String eventIdentifier;
    private String comPortName;
    private String comServerName;

    public CreateIssueEventImpl(String topic, String eventIdentifier) {
        this.timestamp = DEFAULT_TIMESTAMP;
        this.topic = topic;
        this.deviceIdentifier = "1";
        this.comPortName = "TCP";
        this.comServerName = "Rudi.local";
        this.eventIdentifier = eventIdentifier;
    }

    public String getComServerName() {
        return comServerName;
    }

    public String getComPortName() {
        return comPortName;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public String getTopic() {
        return topic;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
