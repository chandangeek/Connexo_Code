/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.SFSKMacCounters;
import com.energyict.dlms.cosem.attributes.SFSKMacCountersAttribute;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKMacCountersMapper extends AbstractDLMSAttributeMapper {

	private final AS220	as220;
	private SFSKMacCounters sFSKMacCounters = null;

	public SFSKMacCountersMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
				SFSKMacCountersAttribute.LOGICAL_NAME.getAttributeNumber(),
				SFSKMacCountersAttribute.SYNCHRONIZATION_REGISTER.getAttributeNumber(),
				SFSKMacCountersAttribute.DESYNCHRONIZATION_LISTING.getAttributeNumber(),
				SFSKMacCountersAttribute.BROADCAST_FRAMES_COUNTER.getAttributeNumber(),
				SFSKMacCountersAttribute.REPETITIONS_COUNTER.getAttributeNumber(),
				SFSKMacCountersAttribute.TRANSMISSIONS_COUNTER.getAttributeNumber(),
				SFSKMacCountersAttribute.CRC_OK_FRAMES_COUNTER.getAttributeNumber(),
				SFSKMacCountersAttribute.CRC_NOK_FRAMES_COUNTER.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		SFSKMacCountersAttribute attribute = SFSKMacCountersAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKMacCounters attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getsFSKMacCounters().asRegisterValue(attributeNr);
	}

	/**
	 * Getter for the {@link SFSKMacCounters} dlms object
	 *
	 * @return
	 * @throws IOException
	 */
	public SFSKMacCounters getsFSKMacCounters() throws IOException {
		if (sFSKMacCounters == null) {
			sFSKMacCounters = getAs220().getCosemObjectFactory().getSFSKMacCounters(getBaseObjectObisCode());
		}
		return sFSKMacCounters;
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
