package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Our default implementation of an {@link EndDeviceEvent}
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:23
 */
public class EndDeviceEventImpl implements EndDeviceEvent {

    private final String code;
    private final Date eventOccurredDate;
    private String reason;
    private String severity;
    private Status status;
    private String type;
    private String issuerId;
    private String issuerTrackingId;
    private String userId;
    private Map<String, String> eventData = new HashMap<>();
    private int logBookId;
    private int logBookPosition;
    private String aliasName;
    private String description;
    private String mRID;
    private String name;

    public EndDeviceEventImpl(String code, Date eventTime) {
        this.code = code;
        this.eventOccurredDate = eventTime;
    }

    @Override
    public Date getCreatedDateTime() {
        return eventOccurredDate;
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
        return issuerTrackingId;
    }

    @Override
    public String getUserID() {
        return userId;
    }

    /*
     * This will contain options
     */
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
        return this.code;
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

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public void setIssuerTrackingId(String issuerTrackingId) {
        this.issuerTrackingId = issuerTrackingId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventData(Map<String, String> eventData) {
        this.eventData.putAll(eventData);
    }

    public void setLogBookId(int logBookId) {
        this.logBookId = logBookId;
    }

    public void setLogBookPosition(int logBookPosition) {
        this.logBookPosition = logBookPosition;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public void setName(String name) {
        this.name = name;
    }
}
