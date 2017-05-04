package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKReportingSystemList;
import com.energyict.dlms.cosem.attributes.SFSKReportingSystemListAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKReportingSystemListMapper extends AbstractDLMSAttributeMapper {

	private CosemObjectFactory cosemObjectFactory;
	private SFSKReportingSystemList sFSKReportingSystemList = null;

	public SFSKReportingSystemListMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
		super(baseObisCode);
		this.cosemObjectFactory = cosemObjectFactory;
	}

	public int[] getSupportedAttributes() {
		return new int[] {
                SFSKReportingSystemListAttribute.LOGICAL_NAME.getAttributeNumber(),
                SFSKReportingSystemListAttribute.REPORTING_SYSTEM_LIST.getAttributeNumber(),
		};
	}

	@Override
	protected RegisterInfo doGetAttributeInfo(int attributeNr) {
        SFSKReportingSystemListAttribute attribute = SFSKReportingSystemListAttribute.findByAttributeNumber(attributeNr);
		if (attribute != null) {
			return new RegisterInfo("SFSKReportingSystemList attribute " + attributeNr + ": " + attribute);
		} else {
			return null;
		}
	}

	@Override
	protected RegisterValue doGetAttributeValue(int attributeNr) throws IOException {
		return getSFSKReportingSystemList().asRegisterValue(attributeNr);
	}

	public SFSKReportingSystemList getSFSKReportingSystemList() throws IOException {
		if (sFSKReportingSystemList == null) {
            sFSKReportingSystemList = getCosemObjectFactory().getSFSKReportingSystemList(getBaseObjectObisCode());
		}
		return sFSKReportingSystemList;
	}

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}