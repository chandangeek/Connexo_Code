package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;

/**
 * Server side functionality for a {@link DeviceProtocolDialectUsagePluggableClass}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/10/12
 * Time: 15:57
 */
public interface ServerDeviceProtocolDialectUsagePluggableClass extends DeviceProtocolDialectUsagePluggableClass, PluggableClassWithRelationSupport {

    /**
     * @return the used DeviceProtocolDialect for this pluggable class
     */
    public DeviceProtocolDialect getDeviceProtocolDialect();

}
