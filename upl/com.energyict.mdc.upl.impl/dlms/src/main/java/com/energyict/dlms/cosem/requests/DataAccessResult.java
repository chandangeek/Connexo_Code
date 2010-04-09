package com.energyict.dlms.cosem.requests;


/**
 * @author jme
 *
 */
public class DataAccessResult implements Field {

	private byte resultCode = 0;

	public byte[] toByteArray() {
		return new byte[] {resultCode};
	}

}
