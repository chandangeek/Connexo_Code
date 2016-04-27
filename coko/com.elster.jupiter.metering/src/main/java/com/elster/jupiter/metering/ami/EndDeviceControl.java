package com.elster.jupiter.metering.ami;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.Status;

import java.time.Instant;
import java.util.Map;

public interface EndDeviceControl extends IdentifiedObject {

	Instant getCreatedDateTime();
	Status getStatus();
	String getType();
	String getIssuerID();
	String getIssuerTrackingID();
	String getUserID();
    Map<String, String> getControlData();
    long getLogBookId();
    int getLogBookPosition();
    String getControlTypeCode();
}
