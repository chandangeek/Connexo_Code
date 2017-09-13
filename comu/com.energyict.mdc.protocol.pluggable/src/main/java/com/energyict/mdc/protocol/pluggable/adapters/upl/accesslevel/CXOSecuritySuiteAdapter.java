/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel;

import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOSecuritySuiteAdapter extends CXODeviceAccessLevelAdapter implements SecuritySuite {

    public static SecuritySuite adaptTo(com.energyict.mdc.protocol.api.security.SecuritySuite connexoSecuritySuite) {
        if (connexoSecuritySuite instanceof UPLSecuritySuiteLevelAdapter) {
            return (SecuritySuite) ((UPLSecuritySuiteLevelAdapter) connexoSecuritySuite).getUplDeviceAccessLevel();
        } else {
            return new CXOSecuritySuiteAdapter(connexoSecuritySuite);
        }
    }

    private CXOSecuritySuiteAdapter(com.energyict.mdc.protocol.api.security.SecuritySuite connexoSecuritySuite) {
        super(connexoSecuritySuite);
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return ((com.energyict.mdc.protocol.api.security.SecuritySuite) this.cxoDeviceAccessLevel)
                .getEncryptionAccessLevels()
                .stream()
                .map(CXOEncryptionLevelAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return ((com.energyict.mdc.protocol.api.security.SecuritySuite) this.cxoDeviceAccessLevel)
                .getAuthenticationAccessLevels()
                .stream()
                .map(CXOAuthenticationLevelAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return ((com.energyict.mdc.protocol.api.security.SecuritySuite) this.cxoDeviceAccessLevel)
                .getRequestSecurityLevels()
                .stream()
                .map(CXORequestSecurityLevelAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return ((com.energyict.mdc.protocol.api.security.SecuritySuite) this.cxoDeviceAccessLevel)
                .getResponseSecurityLevels()
                .stream()
                .map(CXOResponseSecurityLevelAdapter::adaptTo)
                .collect(Collectors.toList());
    }
}