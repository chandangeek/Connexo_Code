package com.elster.jupiter.metering.events;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.util.Map;

public interface EndDeviceEventRecord extends EndDeviceEvent {

    long getProcessingFlags();

    EndDevice getEndDevice();

    EndDeviceEventType getEventType();

    void save();

    void setAliasName(String aliasName);

    void setDescription(String description);

    void setIssuerID(String issuerID);

    void setIssuerTrackingID(String issuerTrackingID);

    void setLogBookId(int logBookId);

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

    void removeProperty(String key);
}
