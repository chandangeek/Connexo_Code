/*
 * RegisterType.java
 *
 * Created on 21 maart 2006, 17:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author koen
 */
abstract public class AbstractRegisterType {

	/** Creates a new instance of RegisterType */
	public AbstractRegisterType() {
	}

	public String toString() {
		return "AbstractRegisterType: bigDecimal=" + getBigDecimal() + ", Date=" + getDate() + ", String=" + getString();
	}

	public BigDecimal getBigDecimal() {
		return null;
	}

	public Date getDate() {
		return null;
	}

	public String getString() {
		return "" + getBigDecimal();
	}
}
