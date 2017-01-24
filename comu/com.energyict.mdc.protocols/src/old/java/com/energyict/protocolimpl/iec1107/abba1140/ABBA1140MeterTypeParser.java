package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.protocol.api.inbound.MeterType;

public class ABBA1140MeterTypeParser {

	private static final int DEBUG = 0;

	private MeterType meterType	= null;
	private String fullId 		= null;
	private String meterVariant	= null;
	private String meterVersion = null;
	private String deviceType	= null;
	private String fixedId		= null;

	/*
	 * Constructors
	 */

	public ABBA1140MeterTypeParser(MeterType meterType) throws Exception {
		setMeterType(meterType);
		parse(getMeterType().getReceivedIdent());
	}

	/*
	 * Private getters, setters and methods
	 */

	private void setDeviceType(String deviceType) {
		if (deviceType.equalsIgnoreCase("050")) {
			this.deviceType = "ABBA1120 [050]";
		} else if (deviceType.equalsIgnoreCase("051")) {
			this.deviceType = "ABBA1140 [051]";
		} else {
			this.deviceType = "Unknown [" + deviceType + "]";
		}
	}

	private void setMeterVariant(String meterVariant) {
		this.meterVariant = meterVariant;
	}

	private void setMeterType(MeterType meterType) {
		this.meterType = meterType;
	}

	private void setMeterVersion(String meterVersion) {
		this.meterVersion = meterVersion;
	}

	private void setFullId(String fullId) {
		this.fullId = fullId;
	}

	private void setFixedId(String fixedId) {
		this.fixedId = fixedId;
	}

	/*
	 * Public methods
	 */

	public void parse(String receivedIdent) throws Exception {
		if (receivedIdent.length() < 15) throw new Exception("Meter receivedIdent is to short!");
		setFullId(receivedIdent.substring(5, 15));

		setFixedId(getFullId().substring(0, 2));
		setDeviceType(getFullId().substring(2, 5));
		setMeterVariant(getFullId().substring(5, 8));
		setMeterVersion(getFullId().substring(8, 10));

		return;
	}

	/*
	 * Public getters and setters
	 */

	public String getDeviceType() {
		return deviceType;
	}

	public String getMeterVersion() {
		return meterVersion;
	}

	public String getMeterVariant() {
		return meterVariant;
	}

	public String getFullId() {
		return fullId;
	}

	public MeterType getMeterType() {
		return meterType;
	}

	public String getFixedId() {
		return fixedId;
	}

	public String toString() {
		String returnValue =
			getDeviceType() + " " +
			getFixedId() + "." +
			getMeterVariant() +	"v" +
			getMeterVersion();
		return returnValue;
	}
}
