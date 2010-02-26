package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;

/**
 * @author jme
 *
 */
public abstract class AbstractPrintableArray extends Array {

	protected abstract AbstractDataType getArrayItem(int itemNumber);

	public AbstractPrintableArray(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	public AbstractPrintableArray(byte[] berEncodedData) throws IOException {
		super(berEncodedData, 0, 0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nrOfDataTypes(); i++) {
			sb.append("[").append(i).append("](");
			try {
				sb.append(getArrayItem(i));
			} catch (Exception e) {
				sb.append("null");
			}
			sb.append(")");
			if (i != (nrOfDataTypes() - 1)) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

}
