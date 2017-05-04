/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.io.IOException;

/**
 * @author jme
 */
public class MacUnsigned32Couples extends AbstractPrintableArray {

	public MacUnsigned32Couples(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	@Override
	protected AbstractDataType getArrayItem(int itemNumber) {
		try {
			return new MacUnsigned32Couple(getDataType(itemNumber).getStructure().getBEREncodedByteArray());
		} catch (IOException e) {
			return null;
		}
	}

}
