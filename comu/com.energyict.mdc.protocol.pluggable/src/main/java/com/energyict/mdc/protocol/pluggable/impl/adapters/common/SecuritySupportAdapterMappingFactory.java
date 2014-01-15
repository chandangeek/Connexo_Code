package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Provides factory services for {@link SecuritySupportAdapterMapping}.
 *
 * Copyrights EnergyICT
 * Date: 12/04/13
 * Time: 11:03
 */
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