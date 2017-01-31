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
public class ElectricalPhase extends TypeEnum {

	private static final int	UNDEFINED	= 0;
	private static final int	PHASE1		= 1;
	private static final int	PHASE2		= 2;
	private static final int	PHASE3		= 3;

	public ElectricalPhase(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	public ElectricalPhase(int value) throws IOException {
		this(new TypeEnum(value));
	}

	public ElectricalPhase(TypeEnum typeEnum) throws IOException {
		this(typeEnum.getBEREncodedByteArray());
	}

	public ElectricalPhase(byte[] berEncodedByteArray) throws IOException {
		this(berEncodedByteArray, 0);
	}

	@Override
	public String toString() {
		switch (getValue()) {
			case UNDEFINED:
				return "UNDEFINED";
			case PHASE1:
				return "PHASE1";
			case PHASE2:
				return "PHASE2";
			case PHASE3:
				return "PHASE3";
			default:
				return "INVALID[" + getValue() + "]!";
		}
	}

}
