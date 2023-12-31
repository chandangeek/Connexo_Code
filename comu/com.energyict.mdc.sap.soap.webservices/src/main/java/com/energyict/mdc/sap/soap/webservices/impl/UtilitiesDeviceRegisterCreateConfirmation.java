/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;

public interface UtilitiesDeviceRegisterCreateConfirmation {
    String NAME = "SAP SmartMeterRegisterCreateConfirmation";


    /**
     * Invoked the SAP utilities device register create request is proceeded
     */
    void call(UtilitiesDeviceRegisterCreateConfirmationMessage msg);
}
