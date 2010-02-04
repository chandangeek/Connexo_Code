/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * @author jme
 *
 */
public class Frequencies extends Array {

	public Frequencies(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nrOfDataTypes(); i++) {
			if (getDataType(i).isStructure()) {
				Structure struct = getDataType(i).getStructure();
				if (struct.nrOfDataTypes() == 2) {
					if (struct.getDataType(0).isUnsigned32() && struct.getDataType(1).isUnsigned32()) {
						sb.append("Fs[").append(i+1).append("]=").append(struct.getDataType(0).getUnsigned32().longValue()).append(", ");
						sb.append("Fm[").append(i+1).append("]=").append(struct.getDataType(1).getUnsigned32().longValue()).append("; ");
					}
				}
			}
		}
		return sb.toString();
	}

}
