package com.elster.protocolimpl.lis200.registers;

import com.elster.protocolimpl.lis200.objects.SimpleObject;

@SuppressWarnings({"unused"})
public class Lis200RegisterN {

	/** The obisCode from the register */
	private final Lis200ObisCode obiscode;
	/** The Object in the meter */
	private final SimpleObject lis200Object;

	/**
	 * @param obisCode
	 *            - the
	 *
	 * @param lis200Object
	 *            - the lis200 object to read
	 */
	public Lis200RegisterN(Lis200ObisCode obisCode, SimpleObject lis200Object) {
		this.obiscode = obisCode;
		this.lis200Object = lis200Object;
	}

	/**
	 * @return the address
	 */
	public SimpleObject getObject() {
		return lis200Object;
	}

	/**
	 * @return the obisCode
	 */
	public Lis200ObisCode getLis200ObisCode() {
		return obiscode;
	}

	/**
	 * Getter for name
	 *
	 * @return description of obis code
	 */
	public String getName() {
		return obiscode.getDesc();
	}
}
