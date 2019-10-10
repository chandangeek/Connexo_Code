/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
@Component(name = "com.energyict.mdc.sap.statuschangerequest.outbound.provider",
        service = {StatusChangeRequestCreateConfirmation.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + StatusChangeRequestCreateConfirmation.SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION})
public class StatusChangeRequestCreateConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut> implements StatusChangeRequestCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public StatusChangeRequestCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port,
            Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return StatusChangeRequestCreateConfirmation.SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION;
    }

    @Override
    public void call(StatusChangeRequestCreateConfirmationMessage confirmationMessage) {
        SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg message = confirmationMessage.getConfirmationMessage();

        SetMultimap<String, String> values = HashMultimap.create();

        confirmationMessage.getConfirmationMessage().getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus().forEach(status->
        {
            values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), status.getUtilitiesDeviceID().getValue());
        });

        using("smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut")
                .withRelatedObject(values)
                .send(message);
    }

    @Override
    public boolean call(StatusChangeRequestCreateConfirmationMessage confirmationMessage, ServiceCall parent) {
        boolean retValue = true;
        List<EndPointConfiguration> endpoints = getEndPointConfigurationsForWebService();
        SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg message = confirmationMessage.getConfirmationMessage();

        SetMultimap<String, String> values = HashMultimap.create();
        confirmationMessage.getConfirmationMessage().getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus().forEach(status->
        {
            values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), status.getUtilitiesDeviceID().getValue());
        });

        Set<EndPointConfiguration> successEndpoints = using("smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut")
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
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}