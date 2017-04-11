package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Created by roghiexternal14 on 4/4/2017.
 */
public class IEC1107SecuritySupportLevelZero extends IEC1107SecuritySupport {

    public IEC1107SecuritySupportLevelZero(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected String getLegacySecurityLevelDefault() {
        return "0";
    }
}
