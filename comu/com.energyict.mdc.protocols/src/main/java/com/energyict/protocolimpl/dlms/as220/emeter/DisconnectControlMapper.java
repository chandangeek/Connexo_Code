package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class DisconnectControlMapper extends AbstractDLMSAttributeMapper {

	private final AS220 as220;
	private Disconnector disconnector = null;

	public DisconnectControlMapper(ObisCode baseObisCode, AS220 as220) {
		super(baseObisCode);
		this.as220 = as220;
	}

	@Override
	public int[] getSupportedAttributes() {
		return new int[] {
				DisconnectControlAttribute.LOGICAL_NAME.getAttributeNumber(),
				DisconnectControlAttribute.OUTPUT_STATE.getAttributeNumber(),
				DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(),
				DisconnectControlAttribute.CONTROL_MODE.getAttributeNumber()
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
		DisconnectControlAttribute attribute = DisconnectControlAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("DisconnectControl attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeId) throws IOException {
		return getDisconnector().asRegisterValue(attributeId);
	}

	/**
	 * Getter for the {@link AS220} protocol
	 *
	 * @return
	 */
	public AS220 getAs220() {
		return as220;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public Disconnector getDisconnector() throws IOException {
		if (disconnector == null) {
			this.disconnector = getAs220().getCosemObjectFactory().getDisconnector(getBaseObjectObisCode());
		}
		return disconnector;
	}

}
