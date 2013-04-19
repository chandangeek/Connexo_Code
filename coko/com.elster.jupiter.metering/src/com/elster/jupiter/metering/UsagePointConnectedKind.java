package com.elster.jupiter.metering;

public enum UsagePointConnectedKind {
	CONNECTED ("connected"),
	PHYSICALLYDISCONNECTED ("physically disconnected"),
	LOGICALLYDISCONNECTED ("logically disconnected");
	
	private final String value;
	
	private UsagePointConnectedKind(String value) {
		this.value = value;
	}
	
	public static UsagePointConnectedKind get(int id) {
		return values()[id-1];
	}
	
	public int getId() {
		return ordinal()+1;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
