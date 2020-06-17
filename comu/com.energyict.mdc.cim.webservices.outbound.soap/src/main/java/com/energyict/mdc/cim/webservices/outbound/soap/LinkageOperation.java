/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

public class LinkageOperation {

	private String meterMrid;
	private String meterName;
	private String usagePointMrid;
	private String usagePointName;
	private String endDeviceMrid;
	private String endDeviceName;

	public String getMeterMrid() {
		return meterMrid;
	}

	public void setMeterMrid(String meterMrid) {
		this.meterMrid = meterMrid;
	}

	public String getMeterName() {
		return meterName;
	}

	public void setMeterName(String meterName) {
		this.meterName = meterName;
	}

	public String getUsagePointMrid() {
		return usagePointMrid;
	}

	public void setUsagePointMrid(String usagePointMrid) {
		this.usagePointMrid = usagePointMrid;
	}

	public String getUsagePointName() {
		return usagePointName;
	}

	public void setUsagePointName(String usagePointName) {
		this.usagePointName = usagePointName;
	}

	public String getEndDeviceMrid() {
		return endDeviceMrid;
	}

	public void setEndDeviceMrid(String endDeviceMrid) {
		this.endDeviceMrid = endDeviceMrid;
	}

	public String getEndDeviceName() {
		return endDeviceName;
	}

	public void setEndDeviceName(String endDeviceName) {
		this.endDeviceName = endDeviceName;
	}
}
