/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterTypeBoolean.java
 *
 * Created on 21 maart 2006, 17:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import java.math.BigDecimal;
/**
 *
 * @author koen
 */
public class RegisterTypeBoolean extends AbstractRegisterType {

	private boolean value;

	/** Creates a new instance of RegisterTypeBoolean */
	public RegisterTypeBoolean(byte[] data) {
		this.setValue(data[0] == 1);
	}

	public boolean isValue() {
		return value;
	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(isValue()?"1":"0");
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public String getString() {
		return ""+isValue();
	}

}
