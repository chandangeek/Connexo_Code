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
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class CalculatedEventRecordImpl implements EndDeviceEventRecord {

    private final Reference<EndDeviceEventType> eventType = ValueReference.absent();
    private final Reference<EndDevice> endDevice = ValueReference.absent();
    private String code;
    private Instant createdDateTime;
    private long createdDateTimeMillis;

    private Instant createTime;
    private Instant modTime;
    private String userName;

    private Map<String, String> properties = new HashMap<>();

    CalculatedEventRecordImpl(Meter meter, String code, Instant createdDateTime, EndDeviceDomain domain) {
        this.eventType.set(new CalculatedEndDeviceEventTypeImpl(code, domain));
        this.endDevice.set(meter);
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
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice.get();
    }

    @Override
    public EndDeviceEventType getEventType() {
        return eventType.get();
    }

    @Override
    public void setAliasName(String aliasName) {

    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void setIssuerID(String issuerID) {

    }

    @Override
    public void setIssuerTrackingID(String issuerTrackingID) {

    }

    @Override
    public void setLogBookId(long logBookId) {

    }

    @Override
    public void setLogBookPosition(int logBookPosition) {

    }

    @Override
    public void setmRID(String mRID) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setProcessingFlags(long processingFlags) {

    }

    @Override
    public void setReason(String reason) {

    }

    @Override
    public void setSeverity(String severity) {

    }

    @Override
    public void setStatus(Status status) {

    }

    @Override
    public void addProperty(String key, String value) {

    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getDeviceEventType() {
        return code;
    }

    @Override
    public void setDeviceEventType(String deviceEventType) {

    }

    @Override
    public void removeProperty(String key) {

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
        return userName;
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
        return eventType.get().getMRID();
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
    public String getName() {
        return null;
    }
}
