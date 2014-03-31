package com.elster.jupiter.issue.share.entity;

//TODO delete this class when events will be sent by MDC
public class CreateIssueEvent {
    private static final long DEFAULT_TIMESTAMP = 1391260995932L;
    private Long timestamp;
    private String topic;
    private String deviceIdentifier;
    private String comPortName;
    private String comServerName;

    public CreateIssueEvent() {
        timestamp = DEFAULT_TIMESTAMP;
        topic = "com/energyict/mdc/isu/connectiontask/FAILURE";
        deviceIdentifier = "1";
        comPortName = "TCP";
        comServerName = "Rudi.local";
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

    public String getTopic() {
        return topic;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
