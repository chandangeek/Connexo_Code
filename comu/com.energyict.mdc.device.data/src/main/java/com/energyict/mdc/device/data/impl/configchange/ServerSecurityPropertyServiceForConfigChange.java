/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;

/**
 * Provides functionality to manipulate the SecurityProperty in order to perform a valid DeviceConfigChange
 */
public interface ServerSecurityPropertyServiceForConfigChange {

    /**
     * Updates the securityProperties which are owned by the origin SecurityPropertySet to the new destination SecurityPropertySet
     *
     * @param device                         the device where the properties will be updated for
     * @param originSecurityPropertySet      the originSecurityPropertySet
     * @param destinationSecurityPropertySet the destinationSecurityPropertySet
     */
    void updateSecurityPropertiesWithNewSecurityPropertySet(Device device, SecurityPropertySet originSecurityPropertySet, SecurityPropertySet destinationSecurityPropertySet);

    /**
     * Deletes all SecurityProperties which are defined for the given SecurityPropertySet on the Device
     *
     * @param device              the device which the securityProperties will be removed
     * @param securityPropertySet the set which modelled the securityProperties
     */
    void deleteSecurityPropertiesFor(Device device, SecurityPropertySet securityPropertySet);

}
