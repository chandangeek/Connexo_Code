package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:42
 */
public class SecuritySupportAdapterMappingImpl implements SecuritySupportAdapterMapping {

    private DataModel dataModel;
    private String deviceProtocolJavaClassName;
    private String securitySupportJavaClassName;

    @Inject
    public SecuritySupportAdapterMappingImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
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

    public void save() {

    }
}