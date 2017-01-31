/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterTypeByte.java
 *
 * Created on 22 maart 2006, 8:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterTypeFloat extends AbstractRegisterType {

	private float value;

	/** Creates a new instance of RegisterTypeByte */
	public RegisterTypeFloat(byte[] data) throws IOException {

		if (data.length == 4) {
			int bits = ((data[0] & 0xff) << 24) |
			((data[1] & 0xff) << 16) |
			((data[2] & 0xff) << 8)  |
			((data[3] & 0xff));

			this.setValue(Float.intBitsToFloat(bits));
		} else {
			throw new IOException("RegisterTypeFloat: data length error, not possible to parse fload (length="+data.length+")!");
		}
	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(""+getValue());
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}
