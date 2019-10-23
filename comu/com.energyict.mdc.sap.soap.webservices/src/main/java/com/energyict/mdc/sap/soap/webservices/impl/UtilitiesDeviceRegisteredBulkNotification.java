/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import java.util.List;

public interface UtilitiesDeviceRegisteredBulkNotification {
    String NAME = "SAP UtilitiesDeviceERPSmartMeterRegisteredBulkNotification_C_Out";


    /**
     * Invoked when the SAP utilities device (bulk) is registered
     * @param deviceIds
     */
    void call(List<String> deviceIds, String uuid);
}
