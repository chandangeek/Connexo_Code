/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class StatusChangeRequestBulkCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    StatusChangeRequestBulkCreateEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn(SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> serviceCallCommands.createServiceCallAndTransition(
                            StatusChangeRequestBulkCreateMessage.builder().from(requestMessage).build()));
            return null;
        });
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}