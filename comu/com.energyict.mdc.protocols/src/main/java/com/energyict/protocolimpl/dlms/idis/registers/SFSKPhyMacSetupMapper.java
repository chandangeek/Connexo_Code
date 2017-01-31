/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKPhyMacSetup;
import com.energyict.dlms.cosem.attributes.SFSKPhyMacSetupAttribute;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKPhyMacSetupMapper extends AbstractDLMSAttributeMapper {

    private SFSKPhyMacSetup sFSKPhyMacSetup = null;
    private CosemObjectFactory cosemObjectFactory = null;

    public SFSKPhyMacSetupMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
        super(baseObisCode);
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public int[] getSupportedAttributes() {
        return new int[]{
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
                SFSKPhyMacSetupAttribute.TRANSMISSION_SPEED.getAttributeNumber()
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
     * Getter for the {@link com.energyict.dlms.cosem.SFSKPhyMacSetup} dlms object
     *
     * @return
     * @throws java.io.IOException
     */
    public SFSKPhyMacSetup getsFSKPhyMacSetup() throws IOException {
        if (sFSKPhyMacSetup == null) {
            sFSKPhyMacSetup = getCosemObjectFactory().getSFSKPhyMacSetup(getBaseObjectObisCode());
        }
        return sFSKPhyMacSetup;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}