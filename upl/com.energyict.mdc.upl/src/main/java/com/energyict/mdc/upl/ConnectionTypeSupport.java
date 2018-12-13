package com.energyict.mdc.upl;

import com.energyict.mdc.upl.io.ConnectionType;

import java.util.List;

/**
 * Defines the supported {@link ConnectionType ConnectionTypes} for this {@link DeviceProtocol}.
 *
 * @author sva
 * @since 4/03/13 - 12:17
 */
public interface ConnectionTypeSupport {

    /**
     * Get a list of all supported {@link ConnectionType ConnectionTypes} for this {@link DeviceProtocol}.
     *
     * @return the list of supported {@link ConnectionType connectionTypes}
     */
    List<? extends ConnectionType> getSupportedConnectionTypes();

}
