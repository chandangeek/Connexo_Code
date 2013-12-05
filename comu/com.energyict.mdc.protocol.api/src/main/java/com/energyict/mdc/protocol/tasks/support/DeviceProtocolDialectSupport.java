package com.energyict.mdc.protocol.tasks.support;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.DeviceProtocolDialect;

import java.util.List;

/**
 * Provides functionality to support a {@link DeviceProtocolDialect}.
 */
public interface DeviceProtocolDialectSupport {

    /**
     * Provides a set of {@link DeviceProtocolDialect}s this protocol can support.
     *
     * @return the supported DeviceProtocolDialects
     */
    public List<DeviceProtocolDialect> getDeviceProtocolDialects();

    /**
     * Add the set of TypedProperties of the specific DeviceProtocolDialect.
     *
     * @param dialectProperties the DeviceProtocolDialectProperties to add to the DeviceProtocol
     */
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties);

}