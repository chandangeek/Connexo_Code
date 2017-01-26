package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import java.util.List;

/**
 * Provides functionality to support a {@link DeviceProtocolDialect}.
 */
public interface DeviceProtocolDialectSupport extends com.energyict.mdc.upl.DeviceProtocolDialectSupport {

    /**
     * Provides a set of {@link DeviceProtocolDialect}s this protocol can support.
     *
     * @return the supported DeviceProtocolDialects
     */
    List<DeviceProtocolDialect> getDeviceProtocolDialects();

}