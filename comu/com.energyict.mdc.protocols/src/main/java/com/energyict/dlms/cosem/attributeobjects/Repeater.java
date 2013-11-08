/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.TypeEnum;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class Repeater extends TypeEnum {

	public static final int	NEVER_REPEAT	= 0;
	public static final int	ALWAYS_REPEAT	= 1;
	public static final int	DYNAMIC_REPEAT	= 2;

	public Repeater(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	public Repeater(int value) {
		super(value);
	}

	public Repeater(TypeEnum typeEnum) {
		super(typeEnum.getValue());
	}

	@Override
	public String toString() {
		switch (getValue()) {
			case NEVER_REPEAT:
				return "NEVER_REPEAT";
			case ALWAYS_REPEAT:
				return "ALWAYS_REPEAT";
			case DYNAMIC_REPEAT:
				return "DYNAMIC_REPEAT";
			default:
				return "INVALID[" + getValue() + "]!";
		}
	}

}
