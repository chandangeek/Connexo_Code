/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface UtilitiesDeviceRegisteredBulkNotification {
    String SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTERED_BULK_NOTIFICATION_C_OUT = "SAP UtilitiesDeviceERPSmartMeterRegisteredBulkNotification_C_Out";


    /**
     * Invoked when the SAP utilities device (bulk) is registered
     * @param deviceIds
     */
    void call(List<String> deviceIds);
}
