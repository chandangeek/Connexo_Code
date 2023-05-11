/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CalculatedEventRecordImpl implements EndDeviceEventRecord {

    private EndDeviceEventType eventType;
    private EndDevice endDevice;
    private String code;
    private Instant createdDateTime;
    private long createdDateTimeMillis;

    CalculatedEventRecordImpl(Meter meter, String code, Instant createdDateTime, EndDeviceDomain domain) {
        this.eventType = new CalculatedEndDeviceEventTypeImpl("0.0.0.0", domain);
        this.endDevice = meter;
        this.code = code;
        this.createdDateTime = createdDateTime;
        this.createdDateTimeMillis = createdDateTime.toEpochMilli();
    }

    public long getCreatedDateTimeMillis() {
        return createdDateTimeMillis;
    }

    @Override
    public long getProcessingFlags() {
        return 0;
    }

    @Override
    public Instant getCreateTime() {
        return null;
    }

    @Override
    public Instant getModTime() {
        return null;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
    }

    @Override
    public EndDeviceEventType getEventType() {
        return eventType;
    }

    @Override
    public void setAliasName(String aliasName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIssuerID(String issuerID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIssuerTrackingID(String issuerTrackingID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogBookPosition(int logBookPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setmRID(String mRID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProcessingFlags(long processingFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReason(String reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSeverity(String severity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(Status status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public String getDeviceEventType() {
        return code;
    }

    @Override
    public void setDeviceEventType(String deviceEventType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateProperties(Map<String, String> props) {
        return false;
    }

    @Override
    public Instant getReadingDateTime() {
        return null;
    }

    @Override
    public void setReadingDateTime(Instant readingDateTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant getCreatedDateTime() {
        return createdDateTime;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public String getSeverity() {
        return null;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getIssuerID() {
        return null;
    }

    @Override
    public String getIssuerTrackingID() {
        return null;
    }

    @Override
    public String getUserID() {
        return null;
    }

    @Override
    public Map<String, String> getEventData() {
        return new HashMap<>();
    }

    @Override
    public long getLogBookId() {
        return 0;
    }

    @Override
    public int getLogBookPosition() {
        return 0;
    }

    @Override
    public String getEventTypeCode() {
        return eventType.getMRID();
    }

    @Override
    public String getAliasName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getMRID() {
        return null;
    }

    @Override
    public String getSerialNumber() {
        return endDevice.getSerialNumber();
    }

    @Override
    public String getName() {
        return null;
    }
}
