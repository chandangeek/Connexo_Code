package com.elster.jupiter.metering.ami;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;

import java.time.Instant;
import java.util.Map;

public interface EndDeviceControlRecord extends EndDeviceControl{

    long getProcessingFlags();

    Instant getCreateTime();

    Instant getModTime();

    EndDevice getEndDevice();

    EndDeviceControlType getControlType();

    void setAliasName(String aliasName);

    void setDescription(String description);

    void setIssuerID(String issuerID);

    void setIssuerTrackingID(String issuerTrackingID);

    void setLogBookId(long logBookId);

    void setLogBookPosition(int logBookPosition);

    void setmRID(String mRID);

    void setName(String name);

    void setProcessingFlags(long processingFlags);

    void setStatus(Status status);

    void addProperty(String key, String value);

    String getProperty(String key);

    Map<String, String> getProperties();

    String getDeviceControlType();

    void setDeviceControlType(String deviceControlType);

    void removeProperty(String key);

	boolean updateProperties(Map<String, String> props);
}
