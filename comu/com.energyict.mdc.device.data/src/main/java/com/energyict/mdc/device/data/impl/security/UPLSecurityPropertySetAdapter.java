package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.CXOEncryptionLevelAdapter;
import com.energyict.mdc.upl.security.SecurityPropertySet;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/02/2017 - 15:47
 */
public class UPLSecurityPropertySetAdapter implements SecurityPropertySet {

    private final com.energyict.mdc.device.config.SecurityPropertySet cxoSecurityPropertySet;

    public UPLSecurityPropertySetAdapter(com.energyict.mdc.device.config.SecurityPropertySet cxoSecurityPropertySet) {
        this.cxoSecurityPropertySet = cxoSecurityPropertySet;
    }

    @Override
    public com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel() {
        return new CXOAuthenticationLevelAdapter(cxoSecurityPropertySet.getAuthenticationDeviceAccessLevel());
    }

    @Override
    public int getAuthenticationDeviceAccessLevelId() {
        return cxoSecurityPropertySet.getAuthenticationDeviceAccessLevel().getId();
    }

    @Override
    public com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel() {
        return new CXOEncryptionLevelAdapter(cxoSecurityPropertySet.getEncryptionDeviceAccessLevel());
    }

    @Override
    public int getEncryptionDeviceAccessLevelId() {
        return cxoSecurityPropertySet.getEncryptionDeviceAccessLevel().getId();
    }

    @Override
    public int getSecuritySuiteId() {
        //TODO port DLMS Suite1/2 security and support in the framework for it
        return 0;   //Always use suite 0 for now
    }

    @Override
    public int getRequestSecurityLevelId() {
        //TODO port DLMS Suite1/2 security and support in the framework for it
        return -1;
    }

    @Override
    public int getResponseSecurityLevelId() {
        //TODO port DLMS Suite1/2 security and support in the framework for it
        return -1;
    }
}