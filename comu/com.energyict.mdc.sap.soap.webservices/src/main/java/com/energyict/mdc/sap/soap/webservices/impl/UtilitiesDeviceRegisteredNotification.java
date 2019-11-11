/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

public interface UtilitiesDeviceRegisteredNotification {
    String NAME = "SAP SmartMeterRegisteredNotification";


    /**
     * Invoked when the SAP utilities device is registered
     * @param deviceId
     */
    void call(String deviceId);
}
