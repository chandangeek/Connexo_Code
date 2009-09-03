package com.energyict.protocolimpl.iec1107.ppm.opus;

/**
 * @author jme
 * @since 3-sep-2009
 */
public class CtrlChar {

	private byte byteValue;
	private String name;
	private String description;

	CtrlChar(int byteValue, String name, String description) {
		this.byteValue = (byte) byteValue;
		this.name = name;
		this.description = description;
	}

	public String toString() {
		return this.name + " " + this.byteValue;
	}

	public byte getByteValue() {
		return this.byteValue;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setByteValue(byte byteValue) {
		this.byteValue = byteValue;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}