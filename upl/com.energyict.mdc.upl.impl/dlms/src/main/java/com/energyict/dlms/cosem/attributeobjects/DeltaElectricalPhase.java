/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * @author jme
 *
 */
public class DeltaElectricalPhase extends TypeEnum {

	public DeltaElectricalPhase(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	@Override
	public String toString() {
		switch (getValue()) {
			case 0:
				return "UNDEFINED";
			case 1:
				return "0 degrees";
			case 2:
				return "60 degrees";
			case 3:
				return "120 degrees";
			case 4:
				return "180 degrees";
			case 5:
				return "-120 degrees";
			case 6:
				return "-60 degrees";
			default:
				return "INVALID[" + getValue() + "]!";
		}
	}

}
