/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;

import javax.inject.Provider;
import java.time.Instant;

public class EndDeviceEventRecordBuilderImpl implements EndDeviceEventRecordBuilder {

    private EndDeviceEventRecordImpl underConstruction;

    public EndDeviceEventRecordBuilderImpl(Provider<EndDeviceEventRecordImpl> deviceEventFactory, EndDevice endDevice, EndDeviceEventType type, Instant date) {
        underConstruction = deviceEventFactory.get().init(endDevice, type, date);
    }

    @Override
    public EndDeviceEventRecordBuilder setAliasName(String aliasName) {
        underConstruction.setAliasName(aliasName);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setDescription(String description) {
        underConstruction.setDescription(description);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setIssuerID(String issuerID) {
        underConstruction.setIssuerID(issuerID);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setIssuerTrackingID(String issuerTrackingID) {
        underConstruction.setIssuerTrackingID(issuerTrackingID);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setLogBookId(long logBookId) {
        underConstruction.setLogBookId(logBookId);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setLogBookPosition(int logBookPosition) {
        underConstruction.setLogBookPosition(logBookPosition);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setmRID(String mRID) {
        underConstruction.setmRID(mRID);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setName(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setProcessingFlags(long processingFlags) {
        underConstruction.setProcessingFlags(processingFlags);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setReason(String reason) {
        underConstruction.setReason(reason);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setSeverity(String severity) {
        underConstruction.setSeverity(severity);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder setStatus(Status status) {
        underConstruction.setStatus(status);
        return this;
    }

    @Override
    public EndDeviceEventRecordBuilder addProperty(String key, String value) {
        underConstruction.addProperty(key, value);
        return this;
    }

    @Override
    public EndDeviceEventRecord create() {
        EndDeviceEventRecordImpl record = this.underConstruction;
        this.underConstruction = null;
        record.save();
        return record;
    }
}
