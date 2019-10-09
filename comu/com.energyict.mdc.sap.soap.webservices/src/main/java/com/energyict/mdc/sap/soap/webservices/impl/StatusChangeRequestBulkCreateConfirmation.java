/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateConfirmationMessage;

public interface StatusChangeRequestBulkCreateConfirmation {

    String NAME = "SAP StatusChangeRequestBulkCreateConfirmation";

    /**
     * Invoked by the service call when the SAP status change request completed or failed
     */
    void call(StatusChangeRequestBulkCreateConfirmationMessage confirmationMessage);

    boolean call(StatusChangeRequestBulkCreateConfirmationMessage confirmationMessage, ServiceCall parent);
}