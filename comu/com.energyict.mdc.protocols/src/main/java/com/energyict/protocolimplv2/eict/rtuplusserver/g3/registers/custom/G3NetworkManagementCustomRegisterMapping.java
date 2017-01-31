/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.dlms.cosem.attributes.G3NetworkManagementAttributes;
import com.energyict.dlms.cosem.attributes.PLCOFDMType2MACSetupAttribute;

import java.io.IOException;

public class G3NetworkManagementCustomRegisterMapping extends CustomRegisterMapping {

    private static final ObisCode obisCode = G3NetworkManagement.getDefaultObisCode();

    public G3NetworkManagementCustomRegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public RegisterValue readRegister() throws IOException {
        G3NetworkManagement g3NetworkManagement = getCosemObjectFactory().getG3NetworkManagement();
        PLCOFDMType2MACSetup plcofdmType2MACSetup = getCosemObjectFactory().getPLCOFDMType2MACSetup();

        RegisterValue result = createAttributesOverview(true,
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.IS_G3_INTERFACE_ENABLED.getAttributeNumber()),
                plcofdmType2MACSetup.getAttrbAbstractDataType(PLCOFDMType2MACSetupAttribute.MAC_PAN_ID.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.AUTOMATIC_ROUTE_MANAGEMENT_ENABLED.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.SNR_ENABLED.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.SNR_INTERVAL.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.SNR_QUIET_TIME.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.SNR_PAYLOAD.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_ENABLED.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_SCHEDULE_INTERVAL.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_BUCKET_SIZE.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_MIN_INACTIVE_METER_TIME.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_MAX_INACTIVE_METER_TIME.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_RETRIES.getAttributeNumber()),
                g3NetworkManagement.getAttrbAbstractDataType(G3NetworkManagementAttributes.KEEP_ALIVE_TIMEOUT.getAttributeNumber())
        );

        return new RegisterValue(result.getObisCode(), result.getText());
    }
}