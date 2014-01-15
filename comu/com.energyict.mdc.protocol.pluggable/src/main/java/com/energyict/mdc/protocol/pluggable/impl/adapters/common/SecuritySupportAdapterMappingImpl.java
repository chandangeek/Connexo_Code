package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:42
 */
public class SecuritySupportAdapterMappingImpl implements SecuritySupportAdapterMapping {

    private String deviceProtocolJavaClassName;
    private String securitySupportJavaClassName;

    public SecuritySupportAdapterMappingImpl() {
        super();
    }

    public SecuritySupportAdapterMappingImpl(String deviceProtocolJavaClassName, String securitySupportJavaClassName) {
        super();
        this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
        this.securitySupportJavaClassName = securitySupportJavaClassName;
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return deviceProtocolJavaClassName;
    }

    @Override
    public String getSecuritySupportJavaClassName() {
        return securitySupportJavaClassName;
    }

}