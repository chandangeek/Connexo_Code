/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * This class extends an {@link Array} to give a fancy toString
 * method when showing PLC channel frequencies.
 *
 * @author jme
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
					for (int j = 0; j < 2; j++) {
						if (struct.getDataType(j).isUnsigned32()) {
							String frequencyType = j == 0 ? "Fs" : "Fm";
							String channel = "[" + (i + 1) + "]";
							long value = struct.getDataType(j).getUnsigned32().longValue();
							sb.append(frequencyType);
							sb.append(channel);
							sb.append("=");
							sb.append(value);
							sb.append(", ");
						}
					}
				}
			}
		}
		return sb.toString();
	}

}
