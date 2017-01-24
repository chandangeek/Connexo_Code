package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKMacCounters;
import com.energyict.dlms.cosem.attributes.SFSKMacCountersAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKMacCountersMapper extends AbstractDLMSAttributeMapper {

	private SFSKMacCounters sFSKMacCounters = null;
    private CosemObjectFactory cosemObjectFactory = null;

	public SFSKMacCountersMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
		super(baseObisCode);
        this.cosemObjectFactory = cosemObjectFactory;
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
	 * Getter for the {@link com.energyict.dlms.cosem.SFSKMacCounters} dlms object
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public SFSKMacCounters getsFSKMacCounters() throws IOException {
		if (sFSKMacCounters == null) {
			sFSKMacCounters = getCosemObjectFactory().getSFSKMacCounters(getBaseObjectObisCode());
		}
		return sFSKMacCounters;
	}

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}