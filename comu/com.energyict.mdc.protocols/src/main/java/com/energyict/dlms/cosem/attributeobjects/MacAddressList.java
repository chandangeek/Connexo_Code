/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class MacAddressList extends AbstractPrintableArray<MacAddress> {

	/**
	 * @param berEncodedData
	 * @param offset
	 * @param level
	 * @throws java.io.IOException
	 */
	public MacAddressList(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	@Override
	protected MacAddress getArrayItem(int itemNumber) {
		try {
			return new MacAddress(getDataType(itemNumber).getBEREncodedByteArray(), 0);
		} catch (IOException e) {
			return null;
		}
	}

}
