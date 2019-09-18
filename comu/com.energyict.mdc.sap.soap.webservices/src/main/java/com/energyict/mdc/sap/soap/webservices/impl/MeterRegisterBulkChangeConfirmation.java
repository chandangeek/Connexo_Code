/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterRegisterBulkChangeConfirmation {
    String NAME = "SapMeterRegisterBulkChangeConfirmation";

    /**
     * Sends confirmation message to SAP
     */
    void call(MeterRegisterBulkChangeConfirmationMessage msg);
}
