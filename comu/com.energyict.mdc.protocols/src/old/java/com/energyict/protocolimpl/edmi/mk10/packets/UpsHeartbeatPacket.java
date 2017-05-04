/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.mk10.packets;

import java.util.Date;

public class UpsHeartbeatPacket extends PushPacket {

	/*
	 * 2 bytes: UPS battery voltage
	 * 4 bytes: Time
	 * 2 bytes: Phase A Raw Current.
	 * 2 bytes: Phase B Raw Current.
	 * 2 bytes: Phase C Raw Current.
	 * 2 bytes: Phase A Raw Voltage.
	 * 2 bytes: Phase B Raw Voltage.
	 * 2 bytes: Phase C Raw Voltage.
	 * 2 bytes: Phase A Current scale factors.
	 * 2 bytes: Phase B Current scale factors.
	 * 2 bytes: Phase C Current scale factors.
	 * 2 bytes: Phase A Voltage scale factors.
	 * 2 bytes: Phase B Voltage scale factors.
	 * 2 bytes: Phase C Voltage scale factors.
	 */

	private int upsBatteryVoltage;
	private Date time;
	private int rawCurrentPhaseA;
	private int rawCurrentPhaseB;
	private int rawCurrentPhaseC;
	private int rawVoltagePhaseA;
	private int rawVoltagePhaseB;
	private int rawVoltagePhaseC;
	private int currentPhaseAScaleFactor;
	private int currentPhaseBScaleFactor;
	private int currentPhaseCScaleFactor;
	private int voltagePhaseAScaleFactor;
	private int voltagePhaseBScaleFactor;
	private int voltagePhaseCScaleFactor;

	public UpsHeartbeatPacket(byte[] packetData) {
		super(packetData);
	}

	public int getUpsBatteryVoltage() {
		return upsBatteryVoltage;
	}

	public Date getTime() {
		return time;
	}

	public int getRawCurrentPhaseA() {
		return rawCurrentPhaseA;
	}

	public int getRawCurrentPhaseB() {
		return rawCurrentPhaseB;
	}

	public int getRawCurrentPhaseC() {
		return rawCurrentPhaseC;
	}

	public int getRawVoltagePhaseA() {
		return rawVoltagePhaseA;
	}

	public int getRawVoltagePhaseB() {
		return rawVoltagePhaseB;
	}

	public int getRawVoltagePhaseC() {
		return rawVoltagePhaseC;
	}

	public int getCurrentPhaseAScaleFactor() {
		return currentPhaseAScaleFactor;
	}

	public int getCurrentPhaseBScaleFactor() {
		return currentPhaseBScaleFactor;
	}

	public int getCurrentPhaseCScaleFactor() {
		return currentPhaseCScaleFactor;
	}

	public int getVoltagePhaseAScaleFactor() {
		return voltagePhaseAScaleFactor;
	}

	public int getVoltagePhaseBScaleFactor() {
		return voltagePhaseBScaleFactor;
	}

	public int getVoltagePhaseCScaleFactor() {
		return voltagePhaseCScaleFactor;
	}

	@Override
	void doParse() {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UpsHeartbeatPacket = ");
		builder.append(getClass().getName());
		builder.append(super.toString());

		builder.append("\n > updBatteryVoltage = ");
		builder.append(upsBatteryVoltage);
		builder.append("\n > time = ");
		builder.append(time);

		builder.append("\n > rawCurrentPhaseA = ");
		builder.append(rawCurrentPhaseA);
		builder.append("\n > rawCurrentPhaseB = ");
		builder.append(rawCurrentPhaseB);
		builder.append("\n > rawCurrentPhaseC = ");
		builder.append(rawCurrentPhaseC);

		builder.append("\n > rawVoltagePhaseA = ");
		builder.append(rawVoltagePhaseA);
		builder.append("\n > rawVoltagePhaseB = ");
		builder.append(rawVoltagePhaseB);
		builder.append("\n > rawVoltagePhaseC = ");
		builder.append(rawVoltagePhaseC);

		builder.append("\n > currentPhaseA = ");
		builder.append(currentPhaseAScaleFactor);
		builder.append("\n > currentPhaseB = ");
		builder.append(currentPhaseBScaleFactor);
		builder.append("\n > currentPhaseC = ");
		builder.append(currentPhaseCScaleFactor);

		builder.append("\n > voltagePhaseA = ");
		builder.append(voltagePhaseAScaleFactor);
		builder.append("\n > voltagePhaseB = ");
		builder.append(voltagePhaseBScaleFactor);
		builder.append("\n > voltagePhaseC = ");
		builder.append(voltagePhaseCScaleFactor);

		return builder.toString();
	}

}
