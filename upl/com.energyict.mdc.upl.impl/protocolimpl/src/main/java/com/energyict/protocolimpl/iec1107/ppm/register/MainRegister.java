package com.energyict.protocolimpl.iec1107.ppm.register;

import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;

/** @author Koen, fbo */

public class MainRegister {

	String name;
	Quantity quantity = null;
	MetaRegister metaRegister = null;

	public MainRegister(MetaRegister metaRegister, Quantity quantity) {
		this.metaRegister = metaRegister;
		this.quantity = quantity;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for property quantity.
	 * 
	 * @return Value of property quantity.
	 */
	public Quantity getQuantity() {
		return quantity;
	}

	/**
	 * Setter for property quantity.
	 * 
	 * @param quantity
	 *            New value of property quantity.
	 */
	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	public RegisterValue toRegisterValue(ObisCode o, Date eventDate, Date toDate) {
		return new RegisterValue(o, quantity, eventDate, null, toDate);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (name != null)
			result.append(name + " ");
		result.append("[" + metaRegister + "]");
		result.append(" quantity = " + this.quantity);
		return result.toString();
	}

}