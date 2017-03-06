package com.energyict.protocolimpl.edmi.common.packets;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class HeartbeatPacket extends PushPacket {

	/*
	 * Variable size: Plant Number string
	 * 16 bytes: GSM IMEI string.
	 * Variable size: GSM SIM card information string.
	 */

	private static final int	OFFSET				= 8;
	private static final int	LENGTH_GSM_IMEI		= 16;
	private static final int	LENGTH_VAR			= 2;

	private String plantNumber;
	private String gsmImei;
	private String gsmSimCardInfo;

	private int pointer = 0;

	public HeartbeatPacket(byte[] packetData) {
		super(packetData);
	}

	public String getPlantNumber() {
		return plantNumber;
	}

	public String getGsmImei() {
		return gsmImei;
	}

	public String getGsmSimCardInfo() {
		return gsmSimCardInfo;
	}

	@Override
	void doParse() {
		if (checkValidLength()) {
			try {
				parsePlantNumber();
				parseGsmImei();
				parseGsmSimCardInfo();
			} catch (IOException e) {
				makeInvalid();
				gsmImei = null;
				plantNumber = null;
				gsmSimCardInfo = null;
			}
		}
	}

	private void parsePlantNumber() throws IOException {
		addPointer(OFFSET);
		plantNumber = readString(getPointer());
	}

	private void parseGsmImei() throws IOException {
		addPointer(getPlantNumber().length() + 1);
		gsmImei = readString(getPointer());
	}

	private void parseGsmSimCardInfo() throws IOException {
		addPointer(getGsmImei().length() + 1);
		gsmSimCardInfo = readString(getPointer());
	}

	private void addPointer(int value) {
		pointer += value;
	}

	private int getPointer() {
		return pointer;
	}

	private boolean checkValidLength() {
		if (getPacketLength() < getMinimumPacketLength()) {
			makeInvalid();
			return false;
		}
		return true;
	}

	private int getMinimumPacketLength() {
		return super.getBasicLength() + LENGTH_GSM_IMEI + LENGTH_VAR;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HeartbeatPacket = ");
		builder.append(getClass().getName());
		builder.append(super.toString());

		builder.append("\n > plantNumber = ");
		builder.append(plantNumber);
		builder.append("\n > gsmImei = ");
		builder.append(gsmImei);
		builder.append("\n > gsmSimCardInfo = ");
		builder.append(gsmSimCardInfo);

		return builder.toString();
	}

}
