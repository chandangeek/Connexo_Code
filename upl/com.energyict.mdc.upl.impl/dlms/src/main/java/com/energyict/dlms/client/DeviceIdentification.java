package com.energyict.dlms.client;

public class DeviceIdentification {

	String serialId;
	int deviceDatabaseId;
	
	public DeviceIdentification(String serialId, int deviceDatabaseId) {
		this.serialId = serialId;
		this.deviceDatabaseId = deviceDatabaseId;
	}

	public String toString() {
		return "DeviceIdentification: serialId="+serialId+", deviceDatabaseId="+deviceDatabaseId;
	}
	
	public String getSerialId() {
		return serialId;
	}

	public int getDeviceDatabaseId() {
		return deviceDatabaseId;
	}
}
