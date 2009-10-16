package com.energyict.genericprotocolimpl.edmi.mk10.packets;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;



public abstract class PushPacket {

	private static final int	LENGTH_PUSH			= 2;
	private static final int	LENGTH_PACKETTYPE	= 2;
	private static final int	LENGTH_CRC			= 2;
	private static final int	LENGTH_SERIAL		= 4;

	private static final int	BYTE_SHIFT_VALUE	= 0x0100;
	private static final int	MAX_BYTE_VALUE		= 0x0FF;

	private byte[] rawData;
	private String serial;
	private PushPacketType pushPacketType;
	private int packetCrc;
	private int actualCrc;
	private boolean validPacket = true;

	abstract void doParse();

	public PushPacket(byte[] packetData) {
		rawData = new byte[packetData.length];
		try {
			ProtocolUtils.arrayCopy(packetData, rawData, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		parse();
	}

	public byte[] getRawData() {
		if (rawData == null) {
			rawData = new byte[0];
		}
		return rawData;
	}

	public boolean isValidPacket() {
		return validPacket;
	}

	public String getSerial() {
		return serial;
	}

	public int getSerialAsInt() {
		return Integer.valueOf(getSerial() == null ? "0" : getSerial());
	}

	public int getPacketLength() {
		return getRawData().length;
	}

	public int getActualCrc() {
		return actualCrc;
	}

	public int getPacketCrc() {
		return packetCrc;
	}

	public PushPacketType getPushPacketType() {
		return pushPacketType;
	}

	private void parse() {
		checkBasicLength();
		if (isValidPacket()) {
			calcCrc();
			parseCrc();
			parsePushPacketType();
			parseSerial();
			doParse();
		}
	}

	private void parseSerial() {
		int sn = 0;
		for (int i = 0; i < LENGTH_SERIAL; i++) {
			int val = getRawData()[LENGTH_PUSH + LENGTH_PACKETTYPE + i] & MAX_BYTE_VALUE;
			sn = (sn * BYTE_SHIFT_VALUE) + val;
		}
		serial = String.valueOf(sn);
	}

	private void parsePushPacketType() {
		int address = getRawData()[LENGTH_PUSH] & MAX_BYTE_VALUE;
		address = (address * BYTE_SHIFT_VALUE) + (getRawData()[LENGTH_PUSH + 1] & MAX_BYTE_VALUE);
		pushPacketType = PushPacketType.getPacketType(address);
	}

	private void parseCrc() {
		packetCrc = getRawData()[getPacketLength() - 1] & MAX_BYTE_VALUE;
		packetCrc += (getRawData()[getPacketLength() - 2] & MAX_BYTE_VALUE) * BYTE_SHIFT_VALUE;
		if (packetCrc != getActualCrc()) {
			makeInvalid();
		}
	}

	private void calcCrc() {
		actualCrc = CRCGenerator.ccittCRC(getRawData(), getPacketLength() - LENGTH_CRC);
	}

	private void checkBasicLength() {
		if (getPacketLength() < this.getBasicLength()) {
			makeInvalid();
		}
	}

	protected void makeInvalid() {
		validPacket = false;
	}

	public static PushPacket getPushPacket(byte[] packetData) {
		UnknownPacket up = new UnknownPacket(packetData);
		switch (up.getPushPacketType()) {
		case ALARM:
			return new AlarmPacket(packetData);
		case COMMISSIONING:
			return new CommissioningPacket(packetData);
		case HEARTBEAT:
			return new HeartbeatPacket(packetData);
		case README:
			return new ReadmePacket(packetData);
		case UPS_HEARTBEAT:
			return new UpsHeartbeatPacket(packetData);
		default:
			return up;
		}
	}

	protected int getBasicLength() {
		return LENGTH_PUSH + LENGTH_PACKETTYPE + LENGTH_SERIAL + LENGTH_CRC;
	}

	protected String readString(int offset) {
		int ptr = offset;
		String returnValue = "";
		byte bt;
		do {
			bt = getRawData()[ptr++];
			if (bt != 0x00) {
				returnValue += new String(new byte[] {bt});
			}
		} while(bt != 0x00);
		return returnValue;
	}

	protected int readInt(int offset, int length) {
		int returnValue = 0;
		for (int i = 0; i < length; i++) {
			returnValue = (returnValue * BYTE_SHIFT_VALUE) + (getRawData()[offset + i] & MAX_BYTE_VALUE);
		}
		return returnValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n > pushPacketType = ");
		builder.append(pushPacketType);
		builder.append("\n > serial = ");
		builder.append(serial);
		builder.append("\n > validPacket = ");
		builder.append(validPacket);
		builder.append("\n > actualCrc = ");
		builder.append(actualCrc);
		builder.append("\n > packetCrc = ");
		builder.append(packetCrc);
		return builder.toString();
	}

}
