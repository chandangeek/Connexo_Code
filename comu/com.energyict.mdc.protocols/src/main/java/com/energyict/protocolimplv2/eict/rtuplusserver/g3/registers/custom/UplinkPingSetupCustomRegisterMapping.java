/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ModemWatchdogConfiguration;
import com.energyict.dlms.cosem.UplinkPingConfiguration;
import com.energyict.dlms.cosem.attributes.ModemWatchdogConfigurationAttributes;
import com.energyict.dlms.cosem.attributes.UplinkPingConfigurationAttributes;

import java.io.IOException;

public class UplinkPingSetupCustomRegisterMapping extends CustomRegisterMapping {

    private final ObisCode obisCode = UplinkPingConfiguration.getDefaultObisCode();

    public UplinkPingSetupCustomRegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public RegisterValue readRegister() throws IOException {
        UplinkPingConfiguration uplinkPingConfiguration = getCosemObjectFactory().getUplinkPingConfiguration();
        ModemWatchdogConfiguration modemWatchdogConfiguration = getCosemObjectFactory().getModemWatchdogConfiguration();

        //Only one attribute (a structure that contains all config parameters)
        return createAttributesOverview(
                uplinkPingConfiguration.getAttrbAbstractDataType(UplinkPingConfigurationAttributes.ENABLE.getAttributeNumber()),
                uplinkPingConfiguration.getAttrbAbstractDataType(UplinkPingConfigurationAttributes.DEST_ADDRESS.getAttributeNumber()),
                uplinkPingConfiguration.getAttrbAbstractDataType(UplinkPingConfigurationAttributes.INTERVAL.getAttributeNumber()),
                uplinkPingConfiguration.getAttrbAbstractDataType(UplinkPingConfigurationAttributes.TIMEOUT.getAttributeNumber()),
                modemWatchdogConfiguration.getAttrbAbstractDataType(ModemWatchdogConfigurationAttributes.IS_ENABLED.getAttributeNumber()),
                modemWatchdogConfiguration.getAttrbAbstractDataType(ModemWatchdogConfigurationAttributes.CONFIG_PARAMETERS.getAttributeNumber())
        );
    }
}