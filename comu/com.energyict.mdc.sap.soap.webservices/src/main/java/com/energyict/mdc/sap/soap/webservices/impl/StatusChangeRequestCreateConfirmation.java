/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.servicecall.ServiceCall;

import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;

public interface StatusChangeRequestCreateConfirmation {

    String SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION = "SAP ConnectionStatusChangeСonfirmation";

    /**
     * Invoked by the service call when the SAP status change request completed or failed
     */
    void call(StatusChangeRequestCreateConfirmationMessage confirmationMessage);

    boolean call(StatusChangeRequestCreateConfirmationMessage confirmationMessage, ServiceCall parent);
}