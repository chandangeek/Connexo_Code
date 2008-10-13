package com.energyict.dlms.client;

public class DeviceChannelName {

	int id; // 0 = device, 1..N = channel
	String name;
	
	public DeviceChannelName(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
