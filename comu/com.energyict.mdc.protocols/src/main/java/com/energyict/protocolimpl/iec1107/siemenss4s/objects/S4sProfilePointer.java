package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

/**
 * Contains the current profilePointer object.
 * (Its more just a value then an object ...)
 * @author gna
 *
 */
public class S4sProfilePointer {

	private byte[] rawBytes;
	
	/**
	 * Creates new instance of the profilePointer object
	 * @param profilePointer the rawData of the pointer
	 */
	public S4sProfilePointer(byte[] profilePointer) {
		this.rawBytes = S4sObjectUtils.revertByteArray(profilePointer);
	}

	/**
	 * @return the current profile pointer
	 */
	public int getCurrentPointer() {
		String str = new String(rawBytes);
		return Integer.parseInt(str, 16)*4;
	}

}
