/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.UtilitiesDeviceID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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
                    .ifPresent(requestMessage -> {
                        SetMultimap<String, String> values = HashMultimap.create();
                        requestMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestMessage()
                                .forEach(msg -> getDeviceConnectionStatuses(msg)
                                        .stream()
                                        .map(StatusChangeRequestBulkCreateEndpoint::getDeviceId)
                                        .flatMap(Functions.asStream())
                                        .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value))
                                );
                        saveRelatedAttributes(values);

                        serviceCallCommands.createServiceCallAndTransition(
                                StatusChangeRequestBulkCreateMessage.builder().from(requestMessage).build());
                    });
            return null;
        });
    }

    private static List<SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts> getDeviceConnectionStatuses(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg changeRequest) {
        return Optional.ofNullable(changeRequest)
                .map(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg::getUtilitiesConnectionStatusChangeRequest)
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
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}