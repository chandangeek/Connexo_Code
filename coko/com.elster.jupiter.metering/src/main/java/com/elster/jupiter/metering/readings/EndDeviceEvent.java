/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.Status;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.Map;

@XmlRootElement
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "xml-type")
public interface EndDeviceEvent extends IdentifiedObject {

    /**
     * This is the datetime of event occurrence in the device
     */
	@XmlAttribute
	Instant getCreatedDateTime();
	@XmlAttribute
	String getReason();
	@XmlAttribute
	String getSeverity();
	@XmlAttribute
	Status getStatus();
	@XmlAttribute
	String getType();
	@XmlAttribute
	String getIssuerID();
	@XmlAttribute
	String getIssuerTrackingID();
	@XmlAttribute
	String getUserID();
	@XmlElement
    Map<String, String> getEventData();
	@XmlAttribute
    long getLogBookId();
	@XmlAttribute
    int getLogBookPosition();

    /**
     * @return CIM EndDeviceEvent code.
     */
	@XmlAttribute
    String getEventTypeCode();
}
