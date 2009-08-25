package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

public class S4sProfilePointer {

	private byte[] rawBytes;
	
	public S4sProfilePointer(byte[] profilePointer) {
		this.rawBytes = S4sObjectUtils.revertByteArray(profilePointer);
	}

	/**
	 * @return the current profile pointer
	 */
	public int getCurrentPointer() {
		String str = new String(rawBytes);
		return Integer.valueOf(str, 16)*4;
	}

}
