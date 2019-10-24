/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;

public interface StatusChangeRequestCancellationConfirmation {

    String NAME = "SAP StatusChangeRequestCancellationConfirmation";

    /**
     * Invoked to send cancelling status change request confirmation
     */
    void call(SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmationMessage);

}