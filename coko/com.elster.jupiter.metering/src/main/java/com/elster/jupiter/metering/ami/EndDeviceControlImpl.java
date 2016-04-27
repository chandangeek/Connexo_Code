package com.elster.jupiter.metering.ami;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Our default implementation of an {@link EndDeviceEvent}
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:23
 */
public class EndDeviceControlImpl implements EndDeviceControl {

    private final Instant eventOccurredDate;
    private final String eventTypeCode;
    private String mrid;
    private String reason;
    private String severity;
    private Status status;
    private String type;
    private String issuerId;
    private String issuerTrackingId;
    private String userId;
    private Map<String, String> eventData = new HashMap<>();
    private long logBookId;
    private int logBookPosition;
    private String aliasName;
    private String description;
    private String name;

    private EndDeviceControlImpl(String eventTypeCode, Instant eventTime) {
        this.eventTypeCode = eventTypeCode;
        this.eventOccurredDate = eventTime;
    }

    public static EndDeviceControlImpl of(String eventTypeCode, Instant eventTime) {
        return new EndDeviceControlImpl(eventTypeCode, eventTime);
    }

    @Override
    public Instant getCreatedDateTime() {
        return eventOccurredDate;
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
    public Map<String, String> getControlData() {
        return eventData;
    }

    @Override
    public long getLogBookId() {
        return logBookId;
    }

    @Override
    public int getLogBookPosition() {
        return logBookPosition;
    }

    @Override
    public String getControlTypeCode() {
        return this.eventTypeCode;
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
        return mrid;
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

    public void setLogBookId(long logBookId) {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setMrid(String mrid) {
        this.mrid = mrid;
    }
}
