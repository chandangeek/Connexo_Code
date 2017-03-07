package com.energyict.protocolimpl.edmi.common.packets;

public class UnknownPacket extends PushPacket {

	public UnknownPacket(byte[] packetData) {
		super(packetData);
	}

	@Override
	void doParse() {
	}
}