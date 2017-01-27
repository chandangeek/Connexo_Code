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

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType16BitSignedInt extends AbstractRegisterType {

	private int value;

	/** Creates a new instance of RegisterType16BitsInt */
	public RegisterType16BitSignedInt(byte[] data) throws IOException {

		setValue(ProtocolUtils.getShort(data,0));

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
