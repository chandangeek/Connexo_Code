/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum MacroPeriod {
	NOTAPPLICABLE(0, "Not applicable"),
	BILLINGPERIOD(8, "Billing period"),
	DAILY(11, "Daily"),
	MONTHLY(13, "Monthly"),
	SEASONAL(22, "Seasonal"),
	WEEKLYS(24, "Weekly"),
	SPECIFIEDPERIOD(32, "Specified period"),
	YEARLY(1001, "Yearly");

	private final int id;
	private final String description;

	MacroPeriod(int id, String description)  {
		this.id = id;
		this.description = description;
	}

	public static MacroPeriod get(int id) {
		for (MacroPeriod each : values()) {
			if (each.getId() == id) {
				return each;
			}
		}
		throw new IllegalEnumValueException(MacroPeriod.class, id);
	}


	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "TimeAttribute " + id + " : " + description;
	}

	public boolean isApplicable() {
		return id != 0;
	}

	public int getId() {
		return id;
	}

}