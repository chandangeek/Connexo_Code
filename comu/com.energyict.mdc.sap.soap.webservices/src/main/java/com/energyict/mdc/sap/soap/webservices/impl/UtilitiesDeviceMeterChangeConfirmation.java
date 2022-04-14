/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange.UtilitiesDeviceMeterChangeConfirmationMessage;

public interface UtilitiesDeviceMeterChangeConfirmation {
    String NAME = "SAP SmartMeterChangeConfirmation";


    /**
     * Invoked when the SAP utilities device create request is proceeded
     */
    void call(UtilitiesDeviceMeterChangeConfirmationMessage msg);
}
