package com.energyict.dlms.cosem.requests;

import com.energyict.dlms.axrdencoding.Unsigned8;

/**
 * @author jme
 *
 */
public class AccessSelector extends Unsigned8 implements Field {

	public AccessSelector(int value) {
		super(value);
	}

	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return new byte[0];
	}

}
