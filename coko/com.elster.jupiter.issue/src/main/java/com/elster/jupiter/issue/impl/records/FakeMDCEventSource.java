package com.elster.jupiter.issue.impl.records;

//TODO delete this class when events will be sent by MDC

/**
 * This class can be used only in test purpose
 */
@Deprecated
public class FakeMDCEventSource {
    private long timestamp;
    private String topic;
    private String comPortName;
    private String comServerName;
    private String deviceIdentifier;
    private long connectionTypePluggableClassId;
    private String comTaskId;
    private long discoveryProtocolId;
    private String masterDeviceId;

    public FakeMDCEventSource(long timestamp, String topic, String comPortName, String comServerName, String deviceIdentifier, long connectionTypePluggableClassId, String comTaskId, long discoveryProtocolId, String masterDeviceId) {
        this.timestamp = timestamp;
        this.topic = topic;
        this.comPortName = comPortName;
        this.comServerName = comServerName;
        this.deviceIdentifier = deviceIdentifier;
        this.connectionTypePluggableClassId = connectionTypePluggableClassId;
        this.comTaskId = comTaskId;
        this.discoveryProtocolId = discoveryProtocolId;
        this.masterDeviceId = masterDeviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public String getComPortName() {
        return comPortName;
    }

    public String getComServerName() {
        return comServerName;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public long getConnectionTypePluggableClassId() {
        return connectionTypePluggableClassId;
    }

    public String getComTaskId() {
        return comTaskId;
    }

    public long getDiscoveryProtocolId() {
        return discoveryProtocolId;
    }

    public String getMasterDeviceId() {
        return masterDeviceId;
    }
}
