/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesDeviceID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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
                    .ifPresent(requestMessage -> {
                        SetMultimap<String, String> values = HashMultimap.create();
                        getDeviceConnectionStatuses(requestMessage.getUtilitiesConnectionStatusChangeRequest())
                                .stream()
                                .map(StatusChangeRequestCreateEndpoint::getDeviceId)
                                .flatMap(Functions.asStream())
                                .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);
                        serviceCallCommands.createServiceCallAndTransition(
                                StatusChangeRequestCreateMessage.builder().from(requestMessage).build());
                    });
            return null;
        });
    }

    private static List<SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts> getDeviceConnectionStatuses(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
        return Optional.ofNullable(changeRequest)
                .map(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq::getDeviceConnectionStatus)
                .orElse(new ArrayList<>());
    }

    private static Optional<String> getDeviceId(SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts status) {
        return Optional.ofNullable(status)
                .map(SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}