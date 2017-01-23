package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * @author jme
 */
public abstract class AbstractDLMSAttributeMapper implements DLMSAttributeMapper {

	private static final int	F_FIELD				= 5;
	private final ObisCode		baseObjectObisCode;

	protected abstract RegisterValue doGetAttributeValue(int attributeId) throws IOException;
	protected abstract RegisterInfo doGetAttributeInfo(int attributeId);

	public AbstractDLMSAttributeMapper(ObisCode baseObisCode) {
		this.baseObjectObisCode = baseObisCode;
	}

	public ObisCode getBaseObjectObisCode() {
		return baseObjectObisCode;
	}

	public boolean isObisCodeMapped(ObisCode obisCode) {
		ObisCode bc = ProtocolTools.setObisCodeField(getBaseObjectObisCode(), F_FIELD, (byte) 0x00);
		ObisCode oc = ProtocolTools.setObisCodeField(obisCode, F_FIELD, (byte) 0x00);
		if (oc.equals(bc) && (getSupportedAttributes() != null)) {
			for (int i = 0; i < getSupportedAttributes().length; i++) {
				if (getSupportedAttributes()[i] == obisCode.getF()) {
					return true;
				}
			}
		}
		return false;
	}

	public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		if (isObisCodeMapped(obisCode)) {
			return doGetAttributeInfo(obisCode.getF());
		} else {
			throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " has no mapped attribute.");
		}
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		if (isObisCodeMapped(obisCode)) {
			RegisterValue register = doGetAttributeValue(obisCode.getF());
			if (register != null) {
				return new RegisterValue(
						obisCode,
						register.getQuantity(),
						register.getEventTime(),
						register.getFromTime(),
						register.getToTime(),
						register.getReadTime(),
						register.getRegisterSpecId(),
						register.getText()
				);
			}
		}
		throw new NoSuchRegisterException("Register with obisCode: " + obisCode.toString() + " has no mapped attribute.");
	}

	public int[] getSupportedAttributes() {
		return new int[0];
	}

}
