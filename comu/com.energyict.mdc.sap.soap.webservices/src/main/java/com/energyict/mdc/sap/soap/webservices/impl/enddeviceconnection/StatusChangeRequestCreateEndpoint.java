/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class StatusChangeRequestCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    StatusChangeRequestCreateEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(
                            StatusChangeRequestCreateMessage.builder().from(requestMessage).setBulk(false).build()));
            return null;
        });
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}