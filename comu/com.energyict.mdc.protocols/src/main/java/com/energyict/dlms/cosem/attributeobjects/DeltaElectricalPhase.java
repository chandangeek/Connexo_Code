/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class DeltaElectricalPhase extends TypeEnum {

	public static final int	UNDEFINED		= 0;
	public static final int	DEGREES_0		= 1;
	public static final int	DEGREES_60		= 2;
	public static final int	DEGREES_120		= 3;
	public static final int	DEGREES_180		= 4;
	public static final int	DEGREES_MIN_120	= 5;
	public static final int	DEGREES_MIN_60	= 6;

	public DeltaElectricalPhase(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	@Override
	public String toString() {
		switch (getValue()) {
			case UNDEFINED:
				return "UNDEFINED";
			case DEGREES_0:
				return "0 degrees";
			case DEGREES_60:
				return "60 degrees";
			case DEGREES_120:
				return "120 degrees";
			case DEGREES_180:
				return "180 degrees";
			case DEGREES_MIN_120:
				return "-120 degrees";
			case DEGREES_MIN_60:
				return "-60 degrees";
			default:
				return "INVALID[" + getValue() + "]!";
		}
	}

}
