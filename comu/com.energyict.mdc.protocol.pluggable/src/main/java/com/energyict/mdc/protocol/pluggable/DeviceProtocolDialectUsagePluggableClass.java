package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.pluggable.PluggableClassWithRelationSupport;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * Models the fact that a Device can use a {@link DeviceProtocolDialect} and
 * his properties defined by the {@link DeviceProtocol}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 1/10/12
 * Time: 15:36
 */
public interface DeviceProtocolDialectUsagePluggableClass extends PluggableClassWithRelationSupport {

    /**
     * The {@link DeviceProtocolDialect} for this DeviceProtocolDialectUsagePluggableClass.
     *
     * @return The DeviceProtocolDialect
     */
    public DeviceProtocolDialect getDeviceProtocolDialect();

}