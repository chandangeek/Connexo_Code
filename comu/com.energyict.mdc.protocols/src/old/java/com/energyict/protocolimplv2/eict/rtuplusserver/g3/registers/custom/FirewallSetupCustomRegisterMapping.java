package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.FirewallSetup;
import com.energyict.dlms.cosem.attributes.FirewallSetupAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Creates an overview (for the text field of a register) of the attributes of the FirewallSetup object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/05/14
 * Time: 15:38
 * Author: khe
 */
public class FirewallSetupCustomRegisterMapping extends CustomRegisterMapping {

    private final ObisCode obisCode = FirewallSetup.getDefaultObisCode();

    public FirewallSetupCustomRegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public RegisterValue readRegister() throws IOException {
        FirewallSetup firewallSetup = getCosemObjectFactory().getFirewallSetup();

        return createAttributesOverview(
                firewallSetup.getAttrbAbstractDataType(FirewallSetupAttributes.IS_ACTIVE.getAttributeNumber()),
                firewallSetup.getAttrbAbstractDataType(FirewallSetupAttributes.LAN_SETUP.getAttributeNumber()),
                firewallSetup.getAttrbAbstractDataType(FirewallSetupAttributes.WAN_SETUP.getAttributeNumber()),
                firewallSetup.getAttrbAbstractDataType(FirewallSetupAttributes.GPRS_SETUP.getAttributeNumber())
        );
    }
}