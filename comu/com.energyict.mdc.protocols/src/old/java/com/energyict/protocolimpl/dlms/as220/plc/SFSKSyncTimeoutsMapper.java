package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.SFSKSyncTimeouts;
import com.energyict.dlms.cosem.attributes.SFSKSyncTimeoutsAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKSyncTimeoutsMapper extends AbstractDLMSAttributeMapper {

	private final AS220	as220;
	private SFSKSyncTimeouts sFSKSyncTimeouts = null;

	public SFSKSyncTimeoutsMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
				SFSKSyncTimeoutsAttribute.LOGICAL_NAME.getAttributeNumber(),
				SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT.getAttributeNumber(),
				SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT.getAttributeNumber(),
				SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED.getAttributeNumber(),
				SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		SFSKSyncTimeoutsAttribute attribute = SFSKSyncTimeoutsAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKSyncTimeouts attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getsFSKSyncTimeouts().asRegisterValue(attributeNr);
	}

	/**
	 * Getter for the {@link SFSKSyncTimeouts} dlms object
	 *
	 * @return
	 * @throws IOException
	 */
	public SFSKSyncTimeouts getsFSKSyncTimeouts() throws IOException {
		if (sFSKSyncTimeouts == null) {
			sFSKSyncTimeouts = getAs220().getCosemObjectFactory().getSFSKSyncTimeouts(getBaseObjectObisCode());
		}
		return sFSKSyncTimeouts;
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
