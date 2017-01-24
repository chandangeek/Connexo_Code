package com.energyict.protocolimpl.iec1107.ppm.register;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;

import java.util.Date;

/** @author Koen, fbo */

public class MainRegister {

	private String name;
	private Quantity quantity = null;
	private MetaRegister metaRegister = null;

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
		return this.quantity;
	}

	/**
	 * Setter for property quantity.
	 *
	 * @param quantity New value of property quantity.
	 */
	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	public RegisterValue toRegisterValue(ObisCode o, Date eventDate, Date toDate) {
		return new RegisterValue(o, this.quantity, eventDate, null, toDate);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (this.name != null) {
			result.append(this.name + " ");
		}
		result.append("[" + this.metaRegister + "]");
		result.append(" quantity = " + this.quantity);
		return result.toString();
	}

}