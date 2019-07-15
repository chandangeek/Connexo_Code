/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UtilitiesDeviceCreateConfirmation {
    String SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_CONFIRMATION_C_OUT = "SAP UtilitiesDeviceERPSmartMeterCreateConfirmation_C_Out";


    /**
     * Invoked when the SAP utilities device create request is proceeded
     */
    void call(UtilsDvceERPSmrtMtrCrteConfMsg msg);
}
