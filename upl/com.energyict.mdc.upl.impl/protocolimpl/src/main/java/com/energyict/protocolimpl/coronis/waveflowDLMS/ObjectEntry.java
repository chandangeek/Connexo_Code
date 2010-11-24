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

	final int getClassId() {
		return classId;
	}
	
}
