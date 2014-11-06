package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

/**
 * Adds behavior to {@link DeviceService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (11:24)
 */
public interface ServerDeviceService extends DeviceService, ReferencePropertySpecFinderProvider {

    /**
     * Creates a new InfoType object based on the given name
     *
     * @param name the name for the InfoType object
     * @return the newly created infoType object
     */
    public InfoType newInfoType(String name);

    /**
     * Finds the infoType which has the given name
     *
     * @param name the name of the InfoType to find
     * @return the requested InfoType or null if none exists with that name
     */
    public InfoType findInfoType(String name);

    /**
     * Finds the infoType with the given unique ID
     *
     * @param infoTypeId the unique ID of the InfoType
     * @return the requested InfoType or null if none exists with that ID
     */
    public InfoType findInfoTypeById(long infoTypeId);

}