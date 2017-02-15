/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

public class Enerium200Register {

	private static final int DEBUG = 0;

	public static final int NON_SIGNED_1_100	= 1;
	public static final int NON_SIGNED_1_10000	= 2;
	public static final int SIGNED				= 3;
	public static final int NON_SIGNED			= 4;
	public static final int F39					= 5;
	public static final int NON_SIGNED_1000		= 6;
	public static final int F15					= 7;
	public static final int SIGNED_1_10000 		= 8;
	public static final int SIGNED_1_100 		= 9;

	private int address;
	private int size;
	private Unit unit;
	private String name;
	private ObisCode obisCode;
	private int scaler;
	private int type;

	/*
	 * Constructors
	 */

	public Enerium200Register(int address, int size, ObisCode obisCode,	Unit unit, String name, int scaler, int type) {
		this.address = address;
		this.size = size;
		this.obisCode = obisCode;
		this.unit = unit;
		this.name = name;
		this.scaler = scaler;
		this.type = type;
	}

	/*
	 * Public getters and setters
	 */

	public int getAddress() {
		return address;
	}
	public int getSize() {
		return size;
	}
	public Unit getUnit() {
		return unit;
	}
	public String getName() {
		return name;
	}
	public ObisCode getObisCode() {
		return obisCode;
	}
	public int getScaler() {
		return scaler;
	}
	public int getType() {
		return type;
	}

	public void setAddress(int address) {
		this.address = address;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setObisCode(ObisCode obisCode) {
		this.obisCode = obisCode;
	}
	public void setScaler(int scaler) {
		this.scaler = scaler;
	}
	public void setType(int type) {
		this.type = type;
	}

    public String toString() {
	    return "Enerium200Register:" +
			    " obisCode=" + getObisCode() +
			    " name=" + getName() +
			    " unit=" + getUnit() +
			    " scaler=" + getScaler() +
			    " address=" + getAddress() +
			    " type=" + getType() +
			    " size=" + getSize();
    }


}
