/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

public interface SecuritySupportAdapterMappingFactory {

    /**
     * Finds the {@link SecuritySupportAdapterMapping} for the specified
     * deviceProtocolJavaClassName and returns the securitySupportJavaClassName.
     *
     * @param deviceProtocolJavaClassName the javaClassName of the deviceProtocol which you need the referenced SecuritySupportJavaClassName from
     * @return the requested SecuritySupport javaClassName
     * @see SecuritySupportAdapterMapping#getSecuritySupportJavaClassName()
     */
    public String getSecuritySupportJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName);

}