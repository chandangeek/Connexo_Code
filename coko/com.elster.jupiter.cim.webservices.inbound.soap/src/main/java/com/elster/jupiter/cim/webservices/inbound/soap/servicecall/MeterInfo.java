/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;

public class MeterInfo {

	private String mrid;
	private String name;
	private String role;

	public MeterInfo() {
		super();
	}

	public MeterInfo(Meter meter) {
		super();
		mrid = meter.getMRID();
		if (!meter.getNames().isEmpty()) {
			name = meter.getNames().get(0).getName();
		}
		role = meter.getRole();
	}

	public String getMrid() {
		return mrid;
	}

	public void setMrid(String mrid) {
		this.mrid = mrid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
