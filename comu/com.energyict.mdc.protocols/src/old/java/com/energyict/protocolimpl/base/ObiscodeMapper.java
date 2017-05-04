/**
 *
 */
package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @author jme
 *
 */
public interface ObiscodeMapper {

	RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException;

	RegisterValue getRegisterValue(ObisCode obisCode) throws IOException;

}
