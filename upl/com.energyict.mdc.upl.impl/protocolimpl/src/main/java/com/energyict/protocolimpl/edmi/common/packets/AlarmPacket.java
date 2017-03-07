package com.energyict.protocolimpl.edmi.common.packets;

public class AlarmPacket extends PushPacket {

	public AlarmPacket(byte[] packetData) {
		super(packetData);
	}

	@Override
	void doParse() {
	}
}