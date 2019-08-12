/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import java.util.List;

/**
 * Defines the supported {@link ConnectionType}s for this {@link DeviceProtocol}.
 *
 * @author sva
 * @since 4/03/13 - 12:17
 */
public interface ConnectionTypeSupport extends com.energyict.mdc.upl.ConnectionTypeSupport {

    /**
     * Get a list of all supported {@link ConnectionType}s for this {@link DeviceProtocol}.
     *
     * @return the list of supported ConnectionTypes
     */
    List<? extends ConnectionType> getSupportedConnectionTypes();

}