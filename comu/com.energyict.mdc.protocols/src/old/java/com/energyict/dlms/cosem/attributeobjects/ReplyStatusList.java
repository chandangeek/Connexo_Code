/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.io.IOException;

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
