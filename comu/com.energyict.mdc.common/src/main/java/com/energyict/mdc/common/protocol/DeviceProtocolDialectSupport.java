/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

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