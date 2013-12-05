package com.energyict.dlms;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

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
