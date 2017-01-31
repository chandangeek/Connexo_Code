/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SFSKSyncTimeouts;
import com.energyict.dlms.cosem.attributes.SFSKSyncTimeoutsAttribute;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;

import java.io.IOException;

public class SFSKSyncTimeoutsMapper extends AbstractDLMSAttributeMapper {

    private SFSKSyncTimeouts sFSKSyncTimeouts = null;
    private CosemObjectFactory cosemObjectFactory = null;

    public SFSKSyncTimeoutsMapper(ObisCode baseObisCode, CosemObjectFactory cosemObjectFactory) {
        super(baseObisCode);
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public int[] getSupportedAttributes() {
        return new int[]{
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
     * Getter for the {@link com.energyict.dlms.cosem.SFSKSyncTimeouts} dlms object
     *
     * @return
     * @throws java.io.IOException
     */
    public SFSKSyncTimeouts getsFSKSyncTimeouts() throws IOException {
        if (sFSKSyncTimeouts == null) {
            sFSKSyncTimeouts = getCosemObjectFactory().getSFSKSyncTimeouts(getBaseObjectObisCode());
        }
        return sFSKSyncTimeouts;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
}