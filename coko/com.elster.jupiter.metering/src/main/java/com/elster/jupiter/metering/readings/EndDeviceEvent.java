package com.elster.jupiter.metering.readings;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.Status;

import java.util.Date;
import java.util.Map;

public interface EndDeviceEvent extends IdentifiedObject {
	Date getCreatedDateTime();
	String getReason();
	String getSeverity();
	Status getStatus();
	String getType();
	String getIssuerID();
	String getIssuerTrackingID();
	String getUserID();
    Map<String, String> getEventData();
    int getLogBookId();
    int getLogBookPosition();

    /**
     * @return CIM EndDeviceEvent code.
     */
    String getEventTypeCode();
}
