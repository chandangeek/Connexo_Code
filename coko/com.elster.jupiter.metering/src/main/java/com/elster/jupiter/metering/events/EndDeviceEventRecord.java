/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.events;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Instant;
import java.util.Map;

public interface EndDeviceEventRecord extends EndDeviceEvent {

    long getProcessingFlags();

    /**
     * This is the date time of event creation in the DB
     */
    Instant getCreateTime();

    /**
     * This is the date time of latest event modification in the DB
     */
    Instant getModTime();

    EndDevice getEndDevice();

    EndDeviceEventType getEventType();

    void setAliasName(String aliasName);

    void setDescription(String description);

    void setIssuerID(String issuerID);

    void setIssuerTrackingID(String issuerTrackingID);

    void setLogBookId(long logBookId);

    void setLogBookPosition(int logBookPosition);

    void setmRID(String mRID);

    void setName(String name);

    void setProcessingFlags(long processingFlags);

    void setReason(String reason);

    void setSeverity(String severity);

    void setStatus(Status status);

    void addProperty(String key, String value);

    String getProperty(String key);

    Map<String, String> getProperties();

    String getDeviceEventType();

    void setDeviceEventType(String deviceEventType);

    void removeProperty(String key);

	boolean updateProperties(Map<String, String> props);
}
