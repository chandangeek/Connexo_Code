/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;

import java.io.IOException;

public class AdditionalInfoCustomRegisterMapping extends CustomRegisterMapping {

    private final ObisCode CUSTOM_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private final ObisCode NTP_SERVER_OBISCODE = ObisCode.fromString("0.0.128.0.10.255");

    public AdditionalInfoCustomRegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return CUSTOM_NAME_OBISCODE;
    }

    @Override
    public RegisterValue readRegister() throws IOException {

        //Only one attribute (a structure that contains all config parameters)
        return createAttributesOverview(
                getCosemObjectFactory().getData(CUSTOM_NAME_OBISCODE).getValueAttr(),
                getCosemObjectFactory().getData(NTP_SERVER_OBISCODE).getValueAttr()
                //dlmsSession.getCosemObjectFactory().getNTPServerAddress().readNTPServerName() //TODO use this line instead of the one above after 8.11.79
        );
    }
}