package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;

/**
 * @author jme
 *
 */
public class ReplyStatusList extends AbstractPrintableArray {

	public ReplyStatusList(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	@Override
	protected AbstractDataType getArrayItem(int itemNumber) {
		try {
			return new ReplyStatus(getDataType(itemNumber).getBEREncodedByteArray(), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

}
