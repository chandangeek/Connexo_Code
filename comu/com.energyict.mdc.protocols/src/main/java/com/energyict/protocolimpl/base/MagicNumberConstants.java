/**
 * 
 */
package com.energyict.protocolimpl.base;

/**
 * Implementation of commonly used magic numbers.
 * This way the usage of 'real' magic numbers can be avoided and all is concentrated in one class.
 * Try to keep to list as short as possible, but if necessary just add your number to the bottom.
 * Note: Hex numbers are started with a 'h'. The numbers '-1', '0' , '1' and '2' are not considered magic numbers
 * 
 * @author gna
 * @since 22-dec-2009
 *
 */
public enum MagicNumberConstants {
	
	three(3),
	four(4),
	five(5),
	six(6),
	seven(7),
	eight(8),
	nine(9),
	ten(10),
	fifteen(15),
	thirty(30),
	sixty(60),
	hundred(100),
	thousand(1000),
	twothousand(2000),
	fivethousand(5000),
	h8000(0x8000);
	
	private int value;
	
	private MagicNumberConstants(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
