/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;

public class UsagePointInfo {

	private String mrid;
	private String name;

	public UsagePointInfo() {
		super();
	}

	public UsagePointInfo(UsagePoint usagePoint) {
		super();
		mrid = usagePoint.getMRID();
		if (!usagePoint.getNames().isEmpty()) {
			name = usagePoint.getNames().get(0).getName();
		}
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
}
