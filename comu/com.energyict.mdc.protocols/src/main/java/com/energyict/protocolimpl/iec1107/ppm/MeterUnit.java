/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.mdc.common.Unit;

/** @author fbo */

public class MeterUnit {

	private String name = "";
	private Unit unit = null;

	public MeterUnit(String name, Unit unit) {
		this.name = name;
		this.unit = unit;
	}

	public String getName() {
		return this.name;
	}

	public Unit getUnit() {
		return this.unit;
	}

	public String toString() {
		return this.name;
	}

}
