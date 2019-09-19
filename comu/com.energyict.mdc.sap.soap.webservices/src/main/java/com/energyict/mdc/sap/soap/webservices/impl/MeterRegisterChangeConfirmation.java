/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeConfirmationMessage;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MeterRegisterChangeConfirmation {
    String NAME = "SAP MeterRegisterChangeConfirmation";

    /**
     * Sends confirmation message to SAP
     */
    void call(MeterRegisterChangeConfirmationMessage msg);
}
