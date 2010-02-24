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
public class Repeater extends TypeEnum {

	public Repeater(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	@Override
	public String toString() {
		switch (getValue()) {
			case 0:
				return "NEVER_REPEAT";
			case 1:
				return "ALWAYS_REPEAT";
			case 2:
				return "DYNAMIC_REPEAT";
			default:
				return "INVALID[" + getValue() + "]!";
		}
	}

}
