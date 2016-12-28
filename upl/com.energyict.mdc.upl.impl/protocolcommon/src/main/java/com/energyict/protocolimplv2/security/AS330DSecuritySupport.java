package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 11:45
 */
public class AS330DSecuritySupport extends DsmrSecuritySupport {

    public AS330DSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected PropertySpec getClientMacAddressPropertySpec(PropertySpecService propertySpecService) {
        return DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(propertySpecService, BigDecimal.valueOf(2));
    }

}