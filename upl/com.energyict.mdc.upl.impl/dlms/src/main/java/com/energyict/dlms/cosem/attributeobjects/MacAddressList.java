/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;

/**
 * @author jme
 *
 */
public class MacAddressList extends AbstractPrintableArray {

	/**
	 * @param berEncodedData
	 * @param offset
	 * @param level
	 * @throws IOException
	 */
	public MacAddressList(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	@Override
	protected AbstractDataType getArrayItem(int itemNumber) {
		try {
			return new MacAddress(getDataType(itemNumber).getBEREncodedByteArray(), 0);
		} catch (IOException e) {
			return null;
		}
	}

}
