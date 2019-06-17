/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class StatusChangeRequestCreateEndpoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    StatusChangeRequestCreateEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(
                        StatusChangeRequestCreateMessage.builder().from(requestMessage).build()));
    }
}