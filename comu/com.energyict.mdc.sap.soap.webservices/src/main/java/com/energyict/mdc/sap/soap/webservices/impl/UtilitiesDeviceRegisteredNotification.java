/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UtilitiesDeviceRegisteredNotification {
    String NAME = "SAP UtilitiesDeviceERPSmartMeterRegisteredNotification_C_Out";


    /**
     * Invoked when the SAP utilities device is registered
     * @param deviceId
     */
    void call(String deviceId);
}
