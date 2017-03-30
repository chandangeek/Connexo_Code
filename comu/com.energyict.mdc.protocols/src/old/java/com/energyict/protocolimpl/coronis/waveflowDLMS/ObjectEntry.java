/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

public class ObjectEntry {
	
	private final String description;
	private final int classId;
	
	public ObjectEntry(String description, int classId) {
		this.description=description;
		this.classId=classId;
	}
	
	final String getDescription() {
		return description;
	}

	public final int getClassId() {
		return classId;
	}
	
}
