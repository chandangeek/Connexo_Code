package com.energyict.mdc.upl;

import com.energyict.mdc.upl.offline.OfflineDeviceContext;

/**
 * Interface used to signal special offline device requirements for an inbound protocol
 */
public interface OfflineDeviceRequirements {

    /**
     * Retrieves the necessary flags for getting the device offline.
     * It's important to select only the required flags (or none), so the database operations are optimized
     *
     * @return the OfflineDeviceContext required by the protocol
     */
    OfflineDeviceContext getOfflineDeviceContext();
}
