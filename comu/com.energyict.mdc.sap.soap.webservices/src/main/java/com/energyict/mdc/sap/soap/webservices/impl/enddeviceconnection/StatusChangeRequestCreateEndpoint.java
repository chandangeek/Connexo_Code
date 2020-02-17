/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class StatusChangeRequestCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final Thesaurus thesaurus;

    @Inject
    StatusChangeRequestCreateEndpoint(ServiceCallCommands serviceCallCommands, Thesaurus thesaurus) {
        this.serviceCallCommands = serviceCallCommands;
        this.thesaurus = thesaurus;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        StatusChangeRequestCreateMessage message = StatusChangeRequestCreateMessage.builder().from(requestMessage).build(thesaurus);
                        SetMultimap<String, String> values = HashMultimap.create();
                        values.putAll(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.getDeviceConnectionStatus().keySet());
                        saveRelatedAttributes(values);
                        serviceCallCommands.createServiceCallAndTransition(message);
                    });
            return null;
        });
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}