package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.BusinessObjectFactory;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
public interface MessageAdapterMappingFactory extends BusinessObjectFactory<MessageAdapterMapping> {

    public String getInsertStatement();

    /**
     * This will clear the cached mapping.
     */
    public void clearCache();

    /**
     * This will cause the factory to load <b>all</b> mappings and cache them.
     * If the requested mapping is included, it will return it.
     *
     * @param deviceProtocolJavaClassName the javaClassName of the deviceProtocol which you need the referenced MessageAdapterMapping from
     * @return the requested MessageAdapter mapping javaClassName
     */
    public String getMessageMappingJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName);
}
