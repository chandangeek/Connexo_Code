package com.energyict.dlms.cosem.requests;

import com.energyict.dlms.axrdencoding.BitString;

/**
 * @author jme
 *
 */
public class InvokeIdAndPriority extends BitString {

	private static final int	BIT_LENGTH	= 8;

	public InvokeIdAndPriority(int value) {
		super(value, BIT_LENGTH);
	}

}
