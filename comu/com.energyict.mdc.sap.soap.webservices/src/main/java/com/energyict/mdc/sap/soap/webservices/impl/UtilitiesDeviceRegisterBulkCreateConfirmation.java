/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UtilitiesDeviceRegisterBulkCreateConfirmation {
    String NAME = "SAP UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmation_C_Out";


    /**
     * Invoked the SAP utilities device register bulk create request is proceeded
     */
    void call(UtilitiesDeviceRegisterCreateConfirmationMessage msg);
}
