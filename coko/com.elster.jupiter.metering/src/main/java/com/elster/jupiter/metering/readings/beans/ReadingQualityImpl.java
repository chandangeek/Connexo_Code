/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.ReadingQuality;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
public class ReadingQualityImpl implements ReadingQuality {
	private final String typeCode;
	private final String comment;
	
	ReadingQualityImpl(String typeCode, String comment) {
		this.typeCode = typeCode;
		this.comment = comment;
	}
	
	@Override
	public String getComment() {
		return comment;
	}
	@Override
	public String getTypeCode() {
		return typeCode;
	}

	@XmlElement(name = "type")
	public String getXmlType() {
		return this.getClass().getName();
	}

	public void setXmlType(String ignore) {
		// For xml unmarshalling purposes only
	}
	
}
