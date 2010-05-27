/**
 *
 */
package com.energyict.protocolimpl.base;

import java.io.IOException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public interface ObiscodeMapper {

	/**
	 * @param obisCode
	 * @return
	 * @throws IOException
	 */
	RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException;

	/**
	 * @param obisCode
	 * @return
	 * @throws IOException
	 */
	RegisterValue getRegisterValue(ObisCode obisCode) throws IOException;

}
