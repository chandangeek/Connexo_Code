/* Created on Nov 19, 2004 3:01:06 PM */
package com.energyict.protocolimpl.iec1107.ppmi1.opus;


/** CtrlChar: for building up messages */
class CtrlChar {

	byte byteValue;
	String name;
	//String description;// KV 22072005 unused code

	CtrlChar(int byteValue, String name, String description) {
		this.byteValue = (byte) byteValue;
		this.name = name;
	//	this.description = description; // KV 22072005 unused code
	}

	public String toString() {
		return name + " " + byteValue;
	}

	/**
	 * Control Characters used by opus protocol 
	 */
	public static CtrlChar SOH = new CtrlChar(0x01, "SOH", "START OF MESSAGE");
	public static CtrlChar ACK = new CtrlChar(0x06, "ACK", "ACKNOWLEDGE");
	public static CtrlChar ETX = new CtrlChar(0x03, "ETX", "END OF TEXT");
	public static CtrlChar NAK = new CtrlChar(0x15, "NAK", "NOT ACKNOWLEDGE");
	public static CtrlChar STX = new CtrlChar(0x02, "STX", "START OF TEXT");
	public static CtrlChar EOT = new CtrlChar(0x04, "EOT", "END OF TRANSMISSION");
	public static CtrlChar CR = new CtrlChar(0x0d, "CR", "Carriage return");
	public static CtrlChar SHARP = new CtrlChar(0x23, "#", "SHARP");
	public static CtrlChar READ = new CtrlChar(0x52, "R", "READ");
	public static CtrlChar WRITE = new CtrlChar(0x57, "W", "WRITE");

}