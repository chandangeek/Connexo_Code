package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.BusinessObjectFactory;

/**
 * This factory will serve functionality to handle
 * {@link SecuritySupportAdapterMapping securitySupportAdapterMappings}
 *
 * Copyrights EnergyICT
 * Date: 12/04/13
 * Time: 11:03
 */
public interface SecuritySupportAdapterMappingFactory extends BusinessObjectFactory<SecuritySupportAdapterMapping> {

    /**
     * This will cause the factory to load <b>all</b> mappings and cache them.
     * If the requested mapping is included, it will return it.
     *
     * @param deviceProtocolJavaClassName the javaClassName of the deviceProtocol which you need the referenced SecuritySupportJavaClassName from
     * @return the requested SecuritySupport javaClassName
     */
    public String getSecuritySupportJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName);

    public String getInsertStatement();

    /**
     * This will clear the cached mapping.
     */
    public void clearCache();
}
