package com.energyict.mdc.protocol.tasks.support;

import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.DeviceProtocol;

import java.util.List;

/**
 * Defines the supported {@link ConnectionType}s for this {@link DeviceProtocol}.
 *
 * @author sva
 * @since 4/03/13 - 12:17
 */
public interface ConnectionTypeSupport {

    /**
     * Get a list of all supported {@link ConnectionType}s for this {@link DeviceProtocol}.
     *
     * @return the list of supported ConnectionTypes
     */
    public List<ConnectionType> getSupportedConnectionTypes();

}