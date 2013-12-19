package com.elster.jupiter.metering.impl.test;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EndDeviceEventImpl implements EndDeviceEvent {

    public String reason;
    public Date createdDateTime;
    public String severity;
    public Status status;
    public String type;
    public String issuerId;
    public String issueTrackingId;
    public Map<String, String> eventData = new HashMap<>();
    public String userId;
    public int logBookId;
    public int logBookPosition;
    public String eventTypeCode;
    public String aliasName;
    public String description;
    public String mRID;
    public String name;

    @Override
    public Date getCreatedDateTime() {
        return createdDateTime;
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
        return status;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIssuerID() {
        return issuerId;
    }

    @Override
    public String getIssuerTrackingID() {
        return issueTrackingId;
    }

    @Override
    public String getUserID() {
        return userId;
    }

    @Override
    public Map<String, String> getEventData() {
        return eventData;
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
    public String getEventTypeCode() {
        return eventTypeCode;
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
}
