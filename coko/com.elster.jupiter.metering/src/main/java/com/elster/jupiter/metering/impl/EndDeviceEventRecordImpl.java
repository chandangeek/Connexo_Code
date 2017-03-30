/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class EndDeviceEventRecordImpl implements EndDeviceEventRecord, PersistenceAware {

    static class EndDeviceEventDetailRecord {
        @SuppressWarnings("unused")
		private EndDeviceEventRecord eventRecord;
        @SuppressWarnings("unused")
		private String eventTypeCode;
        @SuppressWarnings("unused")
		private long endDeviceId;
        @SuppressWarnings("unused")
		private Instant createdDateTime;

        private String key;
        private String value;

        @Inject
        private EndDeviceEventDetailRecord() {
        }

        private EndDeviceEventDetailRecord(EndDeviceEventRecord eventRecord, String key, String value) {
            this.eventRecord = eventRecord;
            eventTypeCode = eventRecord.getEventType().getMRID();
            endDeviceId = eventRecord.getEndDevice().getId();
            createdDateTime = eventRecord.getCreatedDateTime();
            this.key = key;
            this.value = value;
        }
    }

    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String reason;
    private String severity;
    private String issuerID;
    private String issuerTrackingID;
    private String deviceEventType;
    private Status status;
    private long processingFlags;
    private long logBookId;
    private int logBookPosition;
    private Instant createdDateTime;
    
    @SuppressWarnings("unused")
	private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final Reference<EndDeviceEventType> eventType = ValueReference.absent();
    private final Reference<EndDevice> endDevice = ValueReference.absent();
    
    private Map<String, String> properties = new HashMap<>();
    private final List<EndDeviceEventDetailRecord> detailRecords = new ArrayList<>();

    private final DataModel dataModel;
    private final EventService eventService;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndDeviceEventRecordImpl that = (EndDeviceEventRecordImpl) o;

        return createdDateTime.equals(that.createdDateTime) && getEndDevice().equals(that.getEndDevice()) && getEventType().equals(that.getEventType());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEndDevice(),getEventType(),createdDateTime);
    }


    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public Instant getCreatedDateTime() {
        return createdDateTime;
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
    public String getDescription() {
        return description;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice.get();
    }

    @Override
    public Map<String, String> getEventData() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public EndDeviceEventType getEventType() {
        return eventType.get();
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
    public long getLogBookId() {
        return logBookId;
    }

    @Override
    public int getLogBookPosition() {
        return logBookPosition;
    }

    @Override
    public String getEventTypeCode() {
        return eventType.get().getMRID();
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
    public long getProcessingFlags() {
        return processingFlags;
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
    public String getUserID() {
        return userName;
    }

    void save() {
        dataModel.mapper(EndDeviceEventRecord.class).persist(this);
        eventService.postEvent(EventType.END_DEVICE_EVENT_CREATED.topic(), this);
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setIssuerID(String issuerID) {
        this.issuerID = issuerID;
    }

    @Override
    public void setIssuerTrackingID(String issuerTrackingID) {
        this.issuerTrackingID = issuerTrackingID;
    }

    @Override
    public void setLogBookId(long logBookId) {
        this.logBookId = logBookId;
    }

    @Override
    public void setLogBookPosition(int logBookPosition) {
        this.logBookPosition = logBookPosition;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setProcessingFlags(long processingFlags) {
        this.processingFlags = processingFlags;
    }

    @Override
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public String getDeviceEventType() {
        return deviceEventType;
    }

    @Override
    public void setDeviceEventType(String deviceEventType) {
        this.deviceEventType = deviceEventType;
    }

    EndDeviceEventRecordImpl init(EndDevice endDevice, EndDeviceEventType eventType, Instant createdDateTime) {
        this.endDevice.set(endDevice);
        this.createdDateTime = createdDateTime;
        this.eventType.set(eventType);        
        return this;
    }

    @Inject
    EndDeviceEventRecordImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void removeProperty(String key) {
        if (properties.remove(key) != null) {
            for (Iterator<EndDeviceEventDetailRecord> iterator = detailRecords.iterator(); iterator.hasNext(); ) {
                EndDeviceEventDetailRecord next = iterator.next();
                if (next.key.equals(key)) {
                    iterator.remove();
                    return;
                }
            }
        }
    }
    
    @Override 
    public boolean updateProperties(Map<String,String> props) {
    	if (this.properties.equals(props)) {
    		return false;
    	}
    	Set<String> keysToRemove = new HashSet<>(this.properties.keySet()); // make a copy of keySet, as we do not want to change the map when removing.
    	for (Map.Entry<String, String> entry : props.entrySet()) {
    		addProperty(entry.getKey(), entry.getValue());
    		keysToRemove.remove(entry.getKey());
    	}
    	for (String key : keysToRemove) {
    		removeProperty(key);
    	}
    	return true;
    }

    @Override
    public void addProperty(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        if (properties.put(key, value) != null) {
            for (EndDeviceEventDetailRecord detailRecord : detailRecords) {
                if (detailRecord.key.equals(key)) {
                    detailRecord.value = value;
                }
            }
        } else {
            detailRecords.add(new EndDeviceEventDetailRecord(this, key, value));
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void postLoad() {
        for (EndDeviceEventDetailRecord detailRecord : detailRecords) {
            properties.put(detailRecord.key, detailRecord.value);
        }
    }
}
