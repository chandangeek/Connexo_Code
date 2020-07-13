/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "xml-type")
public class EndDeviceEventImpl implements EndDeviceEvent {

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

    private EndDeviceEventImpl(String eventTypeCode, Instant eventTime) {
        this.eventTypeCode = eventTypeCode;
        this.eventOccurredDate = eventTime;
    }

    public static EndDeviceEventImpl of(String eventTypeCode, Instant eventTime) {
        return new EndDeviceEventImpl(eventTypeCode, eventTime);
    }

    public static EndDeviceEventImpl of(String eventTypeCode, String deviceCode, Instant eventTime, String description) {
        EndDeviceEventImpl event = of(eventTypeCode, eventTime);
        event.setType(deviceCode);
        event.setDescription(description);
        return event;
    }

    public static EndDeviceEventImpl copyOf(EndDeviceEvent endDeviceEvent, Instant eventOccurredDate) {
        EndDeviceEventImpl event = new EndDeviceEventImpl(endDeviceEvent.getEventTypeCode(), eventOccurredDate);
        event.setMrid(endDeviceEvent.getMRID());
        event.setReason(endDeviceEvent.getReason());
        event.setSeverity(endDeviceEvent.getSeverity());
        event.setStatus(endDeviceEvent.getStatus());
        event.setType(endDeviceEvent.getType());
        event.setIssuerId(endDeviceEvent.getIssuerID());
        event.setIssuerTrackingId(endDeviceEvent.getIssuerTrackingID());
        event.setUserId(endDeviceEvent.getUserID());
        event.setEventData(endDeviceEvent.getEventData());
        event.setLogBookId(endDeviceEvent.getLogBookId());
        event.setLogBookPosition(endDeviceEvent.getLogBookPosition());
        event.setAliasName(endDeviceEvent.getAliasName());
        event.setDescription(endDeviceEvent.getDescription());
        event.setName(endDeviceEvent.getName());
        return event;
    }

    @Override
    public Instant getCreatedDateTime() {
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
    public long getLogBookId() {
        return logBookId;
    }

    @Override
    public int getLogBookPosition() {
        return logBookPosition;
    }

    @Override
    public String getEventTypeCode() {
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

    @XmlElement(name = "xml-type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
