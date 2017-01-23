package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.SFSKPhyMacSetup;
import com.energyict.dlms.cosem.attributes.SFSKPhyMacSetupAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKPhyMacSetupMapper extends AbstractDLMSAttributeMapper {

	private final AS220	as220;
	private SFSKPhyMacSetup sFSKPhyMacSetup = null;

	public SFSKPhyMacSetupMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
			SFSKPhyMacSetupAttribute.LOGICAL_NAME.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.DELTA_ELECTRICAL_PHASE.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.FREQUENCIES.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.MAC_ADDRESS.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.MAC_GROUP_ADDRESSES.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.REPEATER.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.REPEATER_STATUS.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.MIN_DELTA_CREDIT.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.INITIATOR_MAC_ADDRESS.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.SYNCHRONIZATION_LOCKED.getAttributeNumber(),
			SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		SFSKPhyMacSetupAttribute attribute = SFSKPhyMacSetupAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKPhyMacSetup attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getsFSKPhyMacSetup().asRegisterValue(attributeNr);
	}

	/**
	 * Getter for the {@link SFSKPhyMacSetup} dlms object
	 *
	 * @return
	 * @throws IOException
	 */
	public SFSKPhyMacSetup getsFSKPhyMacSetup() throws IOException {
		if (sFSKPhyMacSetup == null) {
			sFSKPhyMacSetup = getAs220().getCosemObjectFactory().getSFSKPhyMacSetup(getBaseObjectObisCode());
		}
		return sFSKPhyMacSetup;
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
