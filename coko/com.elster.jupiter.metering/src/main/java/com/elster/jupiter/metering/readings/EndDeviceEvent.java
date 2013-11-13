package com.elster.jupiter.metering.readings;

import java.util.Date;

import com.elster.jupiter.cbo.Status;

public interface EndDeviceEvent {
	Date getCreatedDateTime();
	String getReason();
	String getSeverity();
	Status getStatus();
	String getType();
	String getIssuerID();
	String getIssuerTrackingID();
	String getUserID();
}
