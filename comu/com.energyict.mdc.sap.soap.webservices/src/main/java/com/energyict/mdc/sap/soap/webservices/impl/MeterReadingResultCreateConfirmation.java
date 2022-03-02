/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.sendmeterread.MeterReadingResultCreateConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

public interface MeterReadingResultCreateConfirmation {
    String NAME = "SAP SmartMeterReadingResultCreateConfirmation";

    /**
     * Sends confirmation message to SAP
     */
    void call(MeterReadingResultCreateConfirmationMessage msg);
}
