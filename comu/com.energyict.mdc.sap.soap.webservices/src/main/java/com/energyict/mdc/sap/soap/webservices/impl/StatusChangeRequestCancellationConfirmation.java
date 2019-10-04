/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation.StatusChangeRequestCancellationConfirmationMessage;

public interface StatusChangeRequestCancellationConfirmation {

    String NAME = "SAP StatusChangeRequestCancellationConfirmation";

    /**
     * Invoked to cancel status change request
     */
    void call(StatusChangeRequestCancellationConfirmationMessage confirmationMessage);

}