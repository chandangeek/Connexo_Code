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
 *
 *
 * @author khe
 * @since 3/01/2017 - 16:05
 */
public class CXOSecuritySuiteAdapter extends CXODeviceAccessLevelAdapter implements SecuritySuite {

    private final com.energyict.mdc.protocol.api.security.SecuritySuite securitySuite;

    public CXOSecuritySuiteAdapter(com.energyict.mdc.protocol.api.security.SecuritySuite securitySuite) {
        super(securitySuite);
        this.securitySuite = securitySuite;
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return securitySuite.getEncryptionAccessLevels().stream().map(CXOEncryptionLevelAdapter::new).collect(Collectors.toList());
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return securitySuite.getAuthenticationAccessLevels().stream().map(CXOAuthenticationLevelAdapter::new).collect(Collectors.toList());
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return securitySuite.getRequestSecurityLevels().stream().map(CXORequestSecurityLevelAdapter::new).collect(Collectors.toList());
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return securitySuite.getResponseSecurityLevels().stream().map(CXOResponseSecurityLevelAdapter::new).collect(Collectors.toList());
    }
}