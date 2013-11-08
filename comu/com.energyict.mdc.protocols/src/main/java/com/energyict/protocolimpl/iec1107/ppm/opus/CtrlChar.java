package com.energyict.protocolimpl.iec1107.ppm.opus;

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

}