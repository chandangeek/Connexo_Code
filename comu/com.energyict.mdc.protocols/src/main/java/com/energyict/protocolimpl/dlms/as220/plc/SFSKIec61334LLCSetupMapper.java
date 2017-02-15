/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.SFSKIec61334LLCSetup;
import com.energyict.dlms.cosem.attributes.SFSKIec61334LLCSetupAttribute;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKIec61334LLCSetupMapper extends AbstractDLMSAttributeMapper {

	private final AS220	as220;
	private SFSKIec61334LLCSetup sFSKIec61334LLCSetup = null;

	public SFSKIec61334LLCSetupMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
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
	 * Getter for the {@link SFSKIec61334LLCSetup} dlms object
	 *
	 * @return
	 * @throws IOException
	 */
	public SFSKIec61334LLCSetup getsFSKIec61334LLCSetup() throws IOException {
		if (sFSKIec61334LLCSetup == null) {
			sFSKIec61334LLCSetup = getAs220().getCosemObjectFactory().getSFSKIec61334LLCSetup(getBaseObjectObisCode());
		}
		return sFSKIec61334LLCSetup;
	}

	/**
	 * Getter for the {@link AS220} protocol
	 *
	 * @return
	 */
	public AS220 getAs220() {
		return as220;
	}

}
