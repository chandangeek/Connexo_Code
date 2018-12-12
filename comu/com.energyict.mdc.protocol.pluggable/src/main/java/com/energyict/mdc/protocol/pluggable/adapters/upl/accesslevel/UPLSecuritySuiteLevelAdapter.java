package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.elster.jupiter.nls.Thesaurus;
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

    public static SecuritySuite adaptTo(com.energyict.mdc.upl.security.SecuritySuite uplSecuritySuite, Thesaurus thesaurus) {
        if (uplSecuritySuite instanceof CXOSecuritySuiteAdapter) {
            return (SecuritySuite) ((CXOSecuritySuiteAdapter) uplSecuritySuite).getConnexoDeviceAccessLevel();
        } else {
            return new UPLSecuritySuiteLevelAdapter(uplSecuritySuite, thesaurus);
        }
    }

    private UPLSecuritySuiteLevelAdapter(com.energyict.mdc.upl.security.SecuritySuite uplSecuritySuite, Thesaurus thesaurus) {
        super(uplSecuritySuite, thesaurus);
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getEncryptionAccessLevels()
                .stream()
                .map(accessLevel -> UPLEncryptionLevelAdapter.adaptTo(accessLevel, getThesaurus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getAuthenticationAccessLevels()
                .stream()
                .map(accessLevel -> UPLAuthenticationLevelAdapter.adaptTo(accessLevel, getThesaurus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getRequestSecurityLevels()
                .stream()
                .map(accessLevel -> UPLRequestSecurityLevelAdapter.adaptTo(accessLevel, getThesaurus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return ((com.energyict.mdc.upl.security.SecuritySuite) this.uplDeviceAccessLevel)
                .getResponseSecurityLevels()
                .stream()
                .map(accessLevel -> UPLResponseSecurityLevelAdapter.adaptTo(accessLevel, getThesaurus()))
                .collect(Collectors.toList());
    }
}