/**
 * 
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Array;

/**
 * @author jme
 *
 */
public class MacAddressList extends Array {

	/**
	 * @param berEncodedData
	 * @param offset
	 * @param level
	 * @throws IOException
	 */
	public MacAddressList(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	public MacAddress getMacAddress(int index) {
		try {
			return new MacAddress(getDataType(index).getBEREncodedByteArray(), 0);
		} catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nrOfDataTypes(); i++) {
			sb.append("[").append(i).append("]=").append(getMacAddress(i)).append(", ");
		}
		return sb.toString();
	}
	
}
