/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface UtilitiesDeviceRegisteredNotification {
    String NAME = "SAP SmartMeterRegisteredNotification";

    /**
     * Invoked when the SAP utilities device is registered
     *
     * @param sapDeviceId
     */
    void call(String sapDeviceId);

    boolean call(String sapDeviceId, Set<EndPointConfiguration> endPointConfigurations);
}
