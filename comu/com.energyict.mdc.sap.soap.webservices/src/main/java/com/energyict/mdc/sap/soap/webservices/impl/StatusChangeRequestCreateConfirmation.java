/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;

@ProviderType
public interface StatusChangeRequestCreateConfirmation {

    String SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION = "SapStatusChangeRequestCreateConfirmation";

    /**
     * Invoked by the service call when the SAP status change request completed or failed
     */
    void call(StatusChangeRequestCreateConfirmationMessage confirmationMessage);
}