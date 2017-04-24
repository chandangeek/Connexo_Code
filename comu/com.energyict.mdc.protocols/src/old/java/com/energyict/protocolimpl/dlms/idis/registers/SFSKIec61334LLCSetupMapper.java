package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKIec61334LLCSetup;
import com.energyict.dlms.cosem.attributes.SFSKIec61334LLCSetupAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKIec61334LLCSetupMapper extends AbstractDLMSAttributeMapper {

	private CosemObjectFactory cosemObjectFactory;
	private SFSKIec61334LLCSetup sFSKIec61334LLCSetup = null;

	public SFSKIec61334LLCSetupMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
		super(baseObisCode);
		this.cosemObjectFactory = cosemObjectFactory;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
				SFSKIec61334LLCSetupAttribute.LOGICAL_NAME.getAttributeNumber(),
				SFSKIec61334LLCSetupAttribute.MAX_FRAME_LENGTH.getAttributeNumber(),
				SFSKIec61334LLCSetupAttribute.REPLY_STATUS_LIST.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		SFSKIec61334LLCSetupAttribute attribute = SFSKIec61334LLCSetupAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKIec61334LLCSetup attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getsFSKIec61334LLCSetup().asRegisterValue(attributeNr);
	}

	/**
	 * Getter for the {@link com.energyict.dlms.cosem.SFSKIec61334LLCSetup} dlms object
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public SFSKIec61334LLCSetup getsFSKIec61334LLCSetup() throws IOException {
		if (sFSKIec61334LLCSetup == null) {
			sFSKIec61334LLCSetup = getCosemObjectFactory().getSFSKIec61334LLCSetup(getBaseObjectObisCode());
		}
		return sFSKIec61334LLCSetup;
	}

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}