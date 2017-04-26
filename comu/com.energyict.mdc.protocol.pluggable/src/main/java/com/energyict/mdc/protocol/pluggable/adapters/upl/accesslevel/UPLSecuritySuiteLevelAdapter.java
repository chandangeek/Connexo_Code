package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class UPLSecuritySuiteLevelAdapter extends UPLDeviceAccessLevelAdapter implements SecuritySuite {

    public UPLSecuritySuiteLevelAdapter(com.energyict.mdc.upl.security.SecuritySuite uplSecuritySuite) {
        super(uplSecuritySuite);
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getEncryptionAccessLevels()
                .stream()
                .map(UPLEncryptionLevelAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getAuthenticationAccessLevels()
                .stream()
                .map(UPLAuthenticationLevelAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getRequestSecurityLevels()
                .stream()
                .map(UPLRequestSecurityLevelAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getResponseSecurityLevels()
                .stream()
                .map(UPLResponseSecurityLevelAdapter::new)
                .collect(Collectors.toList());
    }
}