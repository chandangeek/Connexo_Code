package com.energyict.protocolimpl.base;

import java.io.IOException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public abstract class AbstractDLMSAttributeMapper implements DLMSAttributeMapper {

	private ObisCode baseObjectObisCode = null;

	public AbstractDLMSAttributeMapper(ObisCode baseObisCode) {
		this.baseObjectObisCode = baseObisCode;
	}

	public ObisCode getBaseObjectObisCode() {
		return baseObjectObisCode;
	}

	public void setBaseObjectObisCode(ObisCode obisCode) {
		this.baseObjectObisCode = obisCode;
	}

	public boolean isObisCodeMapped(ObisCode obisCode) {
		return false;
	}

	public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		return new RegisterInfo(obisCode.toString());
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		return new RegisterValue(obisCode);
	}

}
