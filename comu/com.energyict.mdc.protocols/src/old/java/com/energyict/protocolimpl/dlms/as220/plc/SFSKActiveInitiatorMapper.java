package com.energyict.protocolimpl.dlms.as220.plc;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.SFSKActiveInitiator;
import com.energyict.dlms.cosem.attributes.SFSKActiveInitiatorAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKActiveInitiatorMapper extends AbstractDLMSAttributeMapper {

	private final AS220	as220;
	private SFSKActiveInitiator sFSKActiveInitiator = null;

	public SFSKActiveInitiatorMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
			SFSKActiveInitiatorAttribute.LOGICAL_NAME.getAttributeNumber(),
			SFSKActiveInitiatorAttribute.ACTIVE_INITIATOR.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		SFSKActiveInitiatorAttribute attribute = SFSKActiveInitiatorAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKActiveInitiator attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getsFSKActiveInitiator().asRegisterValue(attributeNr);
	}

	/**
	 * Getter for the {@link SFSKActiveInitiator} dlms object
	 *
	 * @return
	 * @throws IOException
	 */
	public SFSKActiveInitiator getsFSKActiveInitiator() throws IOException {
		if (sFSKActiveInitiator == null) {
			sFSKActiveInitiator = getAs220().getCosemObjectFactory().getSFSKActiveInitiator(getBaseObjectObisCode());
		}
		return sFSKActiveInitiator;
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
