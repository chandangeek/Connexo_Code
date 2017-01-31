/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

public interface DeviceProtocolDialectUsagePluggableClass extends PluggableClass {

    /**
     * The {@link DeviceProtocolDialect} for this DeviceProtocolDialectUsagePluggableClass.
     *
     * @return The DeviceProtocolDialect
     */
    DeviceProtocolDialect getDeviceProtocolDialect();

}