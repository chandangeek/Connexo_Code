/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;

public interface UtilitiesDeviceCreateConfirmation {
    String NAME = "SAP SmartMeterCreateConfirmation";


    /**
     * Invoked when the SAP utilities device create request is proceeded
     */
    void call(UtilitiesDeviceCreateConfirmationMessage msg);
}
