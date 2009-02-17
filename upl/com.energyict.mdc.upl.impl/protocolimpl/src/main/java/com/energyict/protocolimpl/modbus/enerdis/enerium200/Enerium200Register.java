package com.energyict.protocolimpl.modbus.enerdis.enerium200;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

public class Enerium200Register {

	private static final int DEBUG = 0;
	
	public static final int NON_SIGNED_1_100	=	1;
	public static final int NON_SIGNED_1_10000	=	2;
	public static final int SIGNED				=	3;
	public static final int NON_SIGNED			=	4;
	public static final int F39				=	5;
	public static final int NON_SIGNED_1000	=	6;
	public static final int F15				=	7;
	
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

	// TODO Auto-generated Constructors stub

	/*
	 * Private getters, setters and methods
	 */

	// TODO Auto-generated Private getters, setters and methods stub

	/*
	 * Public methods
	 */

	// TODO Auto-generated Public methods stub

	/*
	 * Public getters and setters
	 */

	// TODO Auto-generated Public getters and setters stub

}
