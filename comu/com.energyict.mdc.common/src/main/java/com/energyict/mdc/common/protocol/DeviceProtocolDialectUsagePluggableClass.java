/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.energyict.mdc.common.pluggable.PluggableClass;

public interface DeviceProtocolDialectUsagePluggableClass extends PluggableClass {

    /**
     * The {@link DeviceProtocolDialect} for this DeviceProtocolDialectUsagePluggableClass.
     *
     * @return The DeviceProtocolDialect
     */
    DeviceProtocolDialect getDeviceProtocolDialect();

}