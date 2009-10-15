package com.energyict.genericprotocolimpl.edmi.mk10.packets;

public class HeartbeatPacket extends PushPacket {

	public HeartbeatPacket(byte[] packetData) {
		super(packetData);
	}

	@Override
	protected void doParse() {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HeartbeatPacket = ");
		builder.append(getClass().getName());
		builder.append(super.toString());
		return builder.toString();
	}



}
