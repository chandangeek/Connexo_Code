/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum Aggregate {
	NOTAPPLICABLE (0,"Not applicable"),
	AVERAGE (2,"Average"),
	EXCESS (4,"Excess"),
	HIGHTHRESHOLD(5,"High threshold"),
	LOWTHRESHOLD(7,"Low threshold"),
	MAXIMUM (8,"Maximum"),
	MINIMUM (9,"Minimum"),
	NOMINAL (11,"Nominal"),
	NORMAL (12,"Normal"),
	SECONDMAXIMUM (16,"Second maximum"),
	SECONDMINIMUM (17,"Second minimum"),
	THIRDMAXIMUM (23,"Third maximum"),
	FOURTHMAXIMUM (24,"Fourth maximum"),
	FIFTHMAXIMIMUM (25,"Fifth maximum"),
	SUM (26,"Sum"),
	HIGH(27,"High"),
	LOW(28,"Low");
	
	private final int id;
	private final String description;
	
	Aggregate(int id , String description) {
		this.id = id;
		this.description = description;
	}	
	
	public static Aggregate get(int id) {
		for (Aggregate each : values()) {
			if (each.getId() == id) {
				return each;
			}
		}
		throw new IllegalEnumValueException(Aggregate.class, id);
	}
	
	@Override
	public String toString() {
		return "Data Qualifier " + id + " : " + description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}
	
	public int getId() {
		return id;
	}
}

