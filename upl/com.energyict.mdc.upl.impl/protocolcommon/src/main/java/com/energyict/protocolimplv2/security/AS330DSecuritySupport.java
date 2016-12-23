package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 11:45
 */
public class AS330DSecuritySupport extends DsmrSecuritySupport {

    @Override
    protected PropertySpec getClientMacAddressPropertySpec() {
        return DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(BigDecimal.valueOf(2));
    }

}