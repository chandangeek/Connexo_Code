/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import javax.inject.Inject;

public class SecuritySupportAdapterMappingImpl implements SecuritySupportAdapterMapping {

    private String deviceProtocolJavaClassName;
    private String securitySupportJavaClassName;

    @Inject
    public SecuritySupportAdapterMappingImpl() {
        super();
    }

    public SecuritySupportAdapterMappingImpl(String deviceProtocolJavaClassName, String securitySupportJavaClassName) {
        this();
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