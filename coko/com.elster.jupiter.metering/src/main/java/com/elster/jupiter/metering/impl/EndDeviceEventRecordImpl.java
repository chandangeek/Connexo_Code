package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public final class EndDeviceEventRecordImpl implements EndDeviceEventRecord {

    private String name;
    private String mRID;
    private String description;
    private String aliasName;

    private String reason;
    private String severity;
    private String eventTypeCode;
    private transient EndDeviceEventType eventType;
    private String issuerID;
    private String issuerTrackingID;
    private Status status;
    private long processingFlags;
    private long endDeviceId;
    private transient EndDevice endDevice;
    private int logBookId;
    private int logBookPosition;
    private UtcInstant createdDateTime;
    private Map<String, String> properties;

    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private EndDeviceEventRecordImpl() {
        // for persistence
    }

    EndDeviceEventRecordImpl(EndDevice endDevice, EndDeviceEventType eventType, Date createdDateTime) {
        this.endDevice = endDevice;
        this.endDeviceId = endDevice.getId();
        this.createdDateTime = new UtcInstant(createdDateTime);
        this.eventType = eventType;
        this.eventTypeCode = eventType.getMRID();
    }

    @Override
    public Date getCreatedDateTime() {
        return createdDateTime.toDate();
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getSeverity() {
        return severity;
    }

    @Override
    public Status getStatus() {
        return status == null ? null : status.copy();
    }

    @Override
    public String getType() {
        return getEventType().getName();
    }

    @Override
    public String getIssuerID() {
        return issuerID;
    }

    @Override
    public String getIssuerTrackingID() {
        return issuerTrackingID;
    }

    @Override
    public String getUserID() {
        return userName;
    }

    @Override
    public Map<String, String> getEventData() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public long getProcessingFlags() {
        return processingFlags;
    }

    @Override
    public EndDevice getEndDevice() {
        if (endDevice == null) {
            endDevice = Bus.getOrmClient().getEndDeviceFactory().get(endDeviceId).get();
        }
        return endDevice;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EndDeviceEventType getEventType() {
        if (eventType == null) {
            eventType = Bus.getOrmClient().getEndDeviceEventTypeFactory().get(eventTypeCode).get();
        }
        return eventType;
    }

    @Override
    public int getLogBookId() {
        return logBookId;
    }

    @Override
    public int getLogBookPosition() {
        return logBookPosition;
    }

    @Override
    public void save() {
        Bus.getOrmClient().getEndDeviceEventRecordFactory().persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndDeviceEventRecordImpl that = (EndDeviceEventRecordImpl) o;

        return endDeviceId == that.endDeviceId && createdDateTime.equals(that.createdDateTime) && eventTypeCode.equals(that.eventTypeCode);

    }

    @Override
    public int hashCode() {
        return Objects.hash(endDeviceId, createdDateTime, eventTypeCode);
    }
}
