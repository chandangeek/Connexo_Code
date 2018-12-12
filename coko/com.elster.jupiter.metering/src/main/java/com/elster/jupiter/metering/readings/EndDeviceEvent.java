/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.Status;

import java.time.Instant;
import java.util.Map;

public interface EndDeviceEvent extends IdentifiedObject {

    /**
     * This is the datetime of event occurrence in the device
     */
	Instant getCreatedDateTime();
	String getReason();
	String getSeverity();
	Status getStatus();
	String getType();
	String getIssuerID();
	String getIssuerTrackingID();
	String getUserID();
    Map<String, String> getEventData();
    long getLogBookId();
    int getLogBookPosition();

    /**
     * @return CIM EndDeviceEvent code.
     */
    String getEventTypeCode();
}
