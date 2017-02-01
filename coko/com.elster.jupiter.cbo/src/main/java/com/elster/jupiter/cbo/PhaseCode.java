/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum PhaseCode {
	UNKNOWN("Unknown"),
	ABCN ("ABCN"),
	ABC ("ABC"),
	ABN ("ABN"),
	ACN ("ACN"),
	BCN ("BCN"),
	AB ("AB"),
	AC ("AC"),
	BC ("BC"),
	AN ("AN"),
	BN ("BN"),
	CN ("CN"),
	A ("A"),
	B ("B"),
	C ("C"),
	N ("N"),
	S1N ("s1N"),
	S2N ("s2N"),
	S12N ("s12N"),
	S1 ("s1"),
	S2 ("s2"),
	S12 ("s12");
	
	private final String value;
	
	private PhaseCode(String value) {
		this.value = value;
	}

	public static PhaseCode get(String value) {
		for (PhaseCode each : values()) {
			if (each.value.equals(value)) {
				return each;
			}
		}
		throw new IllegalArgumentException(value);
	}
		
	public String getValue() {
		return  value;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
