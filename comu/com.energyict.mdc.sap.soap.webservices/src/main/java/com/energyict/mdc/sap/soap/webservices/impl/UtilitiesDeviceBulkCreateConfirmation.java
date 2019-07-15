/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UtilitiesDeviceBulkCreateConfirmation {
    String SAP_UTILITIES_DEVICE_ERP_SMART_METER_BULK_CREATE_CONFIRMATION_C_OUT = "SAP UtilitiesDeviceERPSmartMeterBulkCreateConfirmation_C_Out";


    /**
     * Invoked when the SAP utilities device bulk create request is proceeded
     */
    void call(UtilitiesDeviceCreateConfirmationMessage msg);
}
