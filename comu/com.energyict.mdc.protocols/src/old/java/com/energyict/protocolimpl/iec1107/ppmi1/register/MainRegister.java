package com.energyict.protocolimpl.iec1107.ppmi1.register;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ppmi1.MetaRegister;

import java.util.Date;

/**
 * @author Koen, fbo
 *
 */
public class MainRegister {

	private String			name			= null;
	private Quantity		quantity		= null;
	private MetaRegister	metaRegister	= null;

	/**
	 * @param metaRegister
	 * @param quantity
	 */
	public MainRegister(MetaRegister metaRegister, Quantity quantity) {
		this.metaRegister = metaRegister;
		this.quantity = quantity;
	}

	/**
	 * @param name
	 */
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
	 * @param quantity New value of property quantity.
	 */
	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	/**
	 * @param o
	 * @param eventDate
	 * @param toDate
	 * @return
	 */
	public RegisterValue toRegisterValue(ObisCode o, Date eventDate, Date toDate) {
		return new RegisterValue(o, quantity, eventDate, null, toDate);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		if (name != null) {
			result.append(name + " ");
		}
		result.append("[" + metaRegister + "]");
		result.append(" quantity = " + this.quantity);
		return result.toString();
	}

}
