package com.energyict.protocolimpl.iec1107.ppmi1.opus;

/**
 * CtrlChar: for building up messages
 * 
 * @author fbo
 */
public class CtrlChar {

	private byte byteValue;
	private String name;

	/**
	 * @param byteValue
	 * @param name
	 * @param description
	 */
	CtrlChar(int byteValue, String name, String description) {
		this.byteValue = (byte) byteValue;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name + " " + byteValue;
	}

	public byte getByteValue() {
		return this.byteValue;
	}

	public String getName() {
		return this.name;
	}

	public void setByteValue(byte byteValue) {
		this.byteValue = byteValue;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Control Characters used by opus protocol
	 */
	public static final CtrlChar SOH = new CtrlChar(0x01, "SOH", "START OF MESSAGE");
	public static final CtrlChar ACK = new CtrlChar(0x06, "ACK", "ACKNOWLEDGE");
	public static final CtrlChar ETX = new CtrlChar(0x03, "ETX", "END OF TEXT");
	public static final CtrlChar NAK = new CtrlChar(0x15, "NAK", "NOT ACKNOWLEDGE");
	public static final CtrlChar STX = new CtrlChar(0x02, "STX", "START OF TEXT");
	public static final CtrlChar EOT = new CtrlChar(0x04, "EOT", "END OF TRANSMISSION");
	public static final CtrlChar CR = new CtrlChar(0x0d, "CR", "Carriage return");
	public static final CtrlChar SHARP = new CtrlChar(0x23, "#", "SHARP");
	public static final CtrlChar READ = new CtrlChar(0x52, "R", "READ");
	public static final CtrlChar WRITE = new CtrlChar(0x57, "W", "WRITE");

}