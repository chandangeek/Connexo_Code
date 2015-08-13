/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

/**
 * Summary of the available archives
 * 
 * @author gna
 * @since 5-mrt-2010
 *
 */
public enum Archives {

	MONTHLY1(1),
	MEASUREMENT1(2),
	MONTHLY2(3),
	MEASUREMENT2(4),
	LOGBOOK(10),
	CHANGELOG(11),
	DAILY1(13),
	DAILY2(14);
	
	/**
	 * The archive Index
	 */
	private int value;
	
	/**
	 * Private constructor
	 * @param value
	 * 			- the {@link #value} of the enum
	 */
	private Archives(int value){
		this.value = value;
	}
	
	/**
	 * @return the {@link #value}
	 */
	public int getValue(){
		return this.value;
	}
}
