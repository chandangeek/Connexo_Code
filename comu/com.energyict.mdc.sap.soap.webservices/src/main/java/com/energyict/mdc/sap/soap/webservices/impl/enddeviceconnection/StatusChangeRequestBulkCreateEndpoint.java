/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class StatusChangeRequestBulkCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final Thesaurus thesaurus;

    @Inject
    StatusChangeRequestBulkCreateEndpoint(ServiceCallCommands serviceCallCommands, Thesaurus thesaurus) {
        this.serviceCallCommands = serviceCallCommands;
        this.thesaurus = thesaurus;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateRequestCIn(SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        StatusChangeRequestBulkCreateMessage message = StatusChangeRequestBulkCreateMessage.builder(thesaurus).from(requestMessage).build();
                        SetMultimap<String, String> values = HashMultimap.create();
                        message.getRequests().forEach(r -> {
                            values.putAll(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), r.getDeviceConnectionStatus().keySet());
                        });
                        saveRelatedAttributes(values);
                        serviceCallCommands.createServiceCallAndTransition(
                                message);
                    });
            return null;
        });
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}