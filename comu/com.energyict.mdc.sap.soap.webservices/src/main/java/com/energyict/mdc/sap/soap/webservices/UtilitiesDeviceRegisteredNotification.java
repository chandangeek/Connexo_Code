/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import java.util.List;

public interface UtilitiesDeviceRegisteredNotification {
    String NAME = "SAP SmartMeterRegisteredNotification";

    /**
     * Get the registered web service name
     *
     * @return web service name
     */
    String getWebServiceName();

    /**
     * Invoked when the SAP utilities device is registered
     * @param deviceId
     */
    void call(String deviceId);

    void call(String sapDeviceId, List<EndPointConfiguration> endPointConfigurations);
}
