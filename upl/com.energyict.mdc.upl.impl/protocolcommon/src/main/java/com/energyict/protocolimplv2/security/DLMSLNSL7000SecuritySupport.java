package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 30/07/2015 - 11:39
 */
public class DLMSLNSL7000SecuritySupport extends DlmsSecuritySupport {

    public DLMSLNSL7000SecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /**
     * The DLMSLNSL7000 protocol has default SecurityLevel 1.
     * So if there's no SecurityLevel configured, it should be considered as level 1 (this has the same behaviour as 1:0 DLMS: password security)
     */
    @Override
    protected String getLegacySecurityLevelDefault() {
        return "1";
    }
}