package com.elster.jupiter.metering.ami;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
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

public final class EndDeviceControlRecordImpl implements EndDeviceControlRecord, PersistenceAware {

    public static class EndDeviceControlDetailRecord {
        @SuppressWarnings("unused")
		private EndDeviceControlRecord controlRecord;
        @SuppressWarnings("unused")
		private String controlTypeCode;
        @SuppressWarnings("unused")
		private long endDeviceId;
        @SuppressWarnings("unused")
		private Instant createdDateTime;

        private String key;
        private String value;

        @Inject
        private EndDeviceControlDetailRecord() {
        }

        private EndDeviceControlDetailRecord(EndDeviceControlRecord controlRecord, String key, String value) {
            this.controlRecord = controlRecord;
            controlTypeCode = controlRecord.getControlType().getMRID();
            endDeviceId = controlRecord.getEndDevice().getId();
            createdDateTime = controlRecord.getCreatedDateTime();
            this.key = key;
            this.value = value;
        }
    }

    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String issuerID;
    private String issuerTrackingID;
    private String deviceControlType;
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

    private final Reference<EndDeviceControlType> controlType = ValueReference.absent();
    private final Reference<EndDevice> endDevice = ValueReference.absent();

    private Map<String, String> properties = new HashMap<>();
    private final List<EndDeviceControlDetailRecord> detailRecords = new ArrayList<>();

    private final DataModel dataModel;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndDeviceControlRecordImpl that = (EndDeviceControlRecordImpl) o;

        return createdDateTime.equals(that.createdDateTime) && getEndDevice().equals(that.getEndDevice()) && getControlType().equals(that.getControlType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndDevice(), getControlType(),createdDateTime);
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
    public Map<String, String> getControlData() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public EndDeviceControlType getControlType() {
        return controlType.get();
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
    public String getControlTypeCode() {
        return controlType.get().getMRID();
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
    public Status getStatus() {
        return status == null ? null : status.copy();
    }

    @Override
    public String getType() {
        return getControlType().getName();
    }

    @Override
    public String getUserID() {
        return userName;
    }

    void save() {
        dataModel.mapper(EndDeviceControlRecord.class).persist(this);
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
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public String getDeviceControlType() {
        return deviceControlType;
    }

    @Override
    public void setDeviceControlType(String deviceControlType) {
        this.deviceControlType = deviceControlType;
    }

    EndDeviceControlRecordImpl init(EndDevice endDevice, EndDeviceControlType controlType, Instant createdDateTime) {
        this.endDevice.set(endDevice);
        this.createdDateTime = createdDateTime;
        this.controlType.set(controlType);
        return this;
    }

    @Inject
    EndDeviceControlRecordImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void removeProperty(String key) {
        if (properties.remove(key) != null) {
            for (Iterator<EndDeviceControlDetailRecord> iterator = detailRecords.iterator(); iterator.hasNext(); ) {
                EndDeviceControlDetailRecord next = iterator.next();
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
            for (EndDeviceControlDetailRecord detailRecord : detailRecords) {
                if (detailRecord.key.equals(key)) {
                    detailRecord.value = value;
                }
            }
        } else {
            detailRecords.add(new EndDeviceControlDetailRecord(this, key, value));
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
        for (EndDeviceControlDetailRecord detailRecord : detailRecords) {
            properties.put(detailRecord.key, detailRecord.value);
        }
    }
}
