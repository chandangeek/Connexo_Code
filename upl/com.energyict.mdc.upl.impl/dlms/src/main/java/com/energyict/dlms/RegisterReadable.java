package com.energyict.dlms;

import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public interface RegisterReadable {

	/**
	 * @return
	 */
	RegisterValue asRegisterValue();

	/**
	 * @return
	 */
	RegisterValue asRegisterValue(int attributeNumber);

}
