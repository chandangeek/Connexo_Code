package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKActiveInitiator;
import com.energyict.dlms.cosem.attributes.SFSKActiveInitiatorAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKActiveInitiatorMapper extends AbstractDLMSAttributeMapper {

    private SFSKActiveInitiator sFSKActiveInitiator = null;
    private CosemObjectFactory cosemObjectFactory = null;

    public SFSKActiveInitiatorMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
        super(baseObisCode);
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public int[] getSupportedAttributes() {
        return new int[]{
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
     * Getter for the {@link com.energyict.dlms.cosem.SFSKActiveInitiator} dlms object
     *
     * @return
     * @throws java.io.IOException
     */
    public SFSKActiveInitiator getsFSKActiveInitiator() throws IOException {
        if (sFSKActiveInitiator == null) {
            sFSKActiveInitiator = getCosemObjectFactory().getSFSKActiveInitiator(getBaseObjectObisCode());
        }
        return sFSKActiveInitiator;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}