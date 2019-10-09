/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.ws.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Component(name = "com.energyict.mdc.sap.statuschangerequestbulk.outbound.provider",
        service = {StatusChangeRequestCreateConfirmation.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + StatusChangeRequestBulkCreateConfirmation.NAME})
public class StatusChangeRequestBulkCreateConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut> implements StatusChangeRequestBulkCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public StatusChangeRequestBulkCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut port,
            Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeSmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return StatusChangeRequestBulkCreateConfirmation.NAME;
    }

    @Override
    public void call(StatusChangeRequestBulkCreateConfirmationMessage confirmationMessage) {
        SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg message = confirmationMessage.getConfirmationMessage();
        using("smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut")
                .send(message);
    }

    @Override
    public boolean call(StatusChangeRequestBulkCreateConfirmationMessage confirmationMessage, ServiceCall parent) {
        boolean retValue = true;
        List<EndPointConfiguration> endpoints = getEndPointConfigurationsForWebService();
        SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg message = confirmationMessage.getConfirmationMessage();

        Set<EndPointConfiguration> successEndpoints = using("smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut")
                .toEndpoints(endpoints)
                .send(message).keySet();

        endpoints.removeAll(successEndpoints);
        if (!endpoints.isEmpty()) {
            retValue = false;
            parent.log(LogLevel.INFO, "Failed to send confirmation to the following endpoints: " + endpoints.stream()
                    .map(EndPointConfiguration::getName)
                    .collect(Collectors.joining(", ")));
        }

        if (!successEndpoints.isEmpty()) {
            parent.log(LogLevel.INFO, "Sent confirmation to the following endpoints: " + successEndpoints.stream()
                    .map(EndPointConfiguration::getName)
                    .collect(Collectors.joining(", ")));
        }else{
            retValue = false;
        }
        return retValue;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}