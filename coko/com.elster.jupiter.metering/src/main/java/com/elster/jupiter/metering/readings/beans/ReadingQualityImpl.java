/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.ReadingQuality;

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
	
	
}
