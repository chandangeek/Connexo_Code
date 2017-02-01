/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum Accumulation {
	NOTAPPLICABLE (0,"Not applicable",false),
	BULKQUANTITY (1,"Bulk quantity",true),
	CONTINUOUSCUMULATIVE (2,"Continuous cumulative",true),
	CUMULATIVE (3,"Cumulative",true),
	DELTADELTA (4,"Delta data",false),
	INDICATING(6,"Indicating",false),
	SUMMATION(9,"Summation",true),
	TIMEDELAY(10,"Time delay",false),
	INSTANTANEOUS(12,"Instantaneous",false),
	LATCHINGQUANTITY(13,"Latching quantity",true),
	BOUNDEDQUANTITY(14,"Bounded quantity", true);
	
	private final int id;
	private final String description;
	private final boolean cumulative;
	
	Accumulation(int id, String description, boolean cumulative) {
		this.id = id;
		this.description = description;
		this.cumulative = cumulative;
	}
	
	public static Accumulation get(int id) {
		for (Accumulation each : values()) {
			if (each.id == id) {
                return each;
            }
		}
        throw new IllegalEnumValueException(Accumulation.class, id);
	}
	
	@Override
	public String toString() {
		return "Accumulation " + id + " : " + description;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}
	
	public boolean isCumulative() {
		return cumulative;
	}
}
