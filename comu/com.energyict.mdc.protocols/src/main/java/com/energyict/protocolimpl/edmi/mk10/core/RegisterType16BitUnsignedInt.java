/*
 * RegisterType16BitsInt.java
 *
 * Created on 22 maart 2006, 9:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author koen
 */
public class RegisterType16BitUnsignedInt extends AbstractRegisterType {

	private int value;

	/** Creates a new instance of RegisterType16BitsInt */
	public RegisterType16BitUnsignedInt(byte[] data) throws IOException {

		setValue(ProtocolUtils.getInt(data,0,2));

	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(""+value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
