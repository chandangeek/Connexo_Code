package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;

import java.util.Arrays;
import java.util.List;

/**
 * This is the same as DlmsSecuritySupport, but without the Manufacturer specific authentication level.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/11/13
 * Time: 9:27
 * Author: khe
 */
public class DsmrSecuritySupport extends DlmsSecuritySupport {

    public DsmrSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LowLevelAuthentication(),
                new Md5Authentication(),
                new Sha1Authentication(),
                new GmacAuthentication());
    }
}
