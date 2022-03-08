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
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UtilitiesDeviceID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Component(name = "com.energyict.mdc.sap.statuschangerequestbulk.outbound.provider",
        service = {StatusChangeRequestBulkCreateConfirmation.class, OutboundSoapEndPointProvider.class}, immediate = true,
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

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
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
        SetMultimap<String, String> values = HashMultimap.create();
        getDeviceConfirmationMessages(message)
                .forEach(msg -> getDeviceConnectionStatuses(msg.getUtilitiesConnectionStatusChangeRequest())
                        .stream()
                        .map(StatusChangeRequestBulkCreateConfirmationProvider::getDeviceId)
                        .flatMap(Functions.asStream())
                        .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value))
                );
        using("smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut")
                .withRelatedAttributes(values)
                .send(message);
    }

    @Override
    public boolean call(StatusChangeRequestBulkCreateConfirmationMessage confirmationMessage, ServiceCall parent) {
        boolean retValue = true;
        List<EndPointConfiguration> endpoints = getEndPointConfigurationsForWebService();
        SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg message = confirmationMessage.getConfirmationMessage();
        SetMultimap<String, String> values = HashMultimap.create();
        getDeviceConfirmationMessages(message)
                .forEach(msg -> getDeviceConnectionStatuses(msg.getUtilitiesConnectionStatusChangeRequest())
                        .stream()
                        .map(StatusChangeRequestBulkCreateConfirmationProvider::getDeviceId)
                        .flatMap(Functions.asStream())
                        .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value))
                );

        Set<EndPointConfiguration> successEndpoints = using("smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut")
                .toEndpoints(endpoints)
                .withRelatedAttributes(values)
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
        } else {
            retValue = false;
        }
        return retValue;
    }

    private static List<SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg> getDeviceConfirmationMessages(SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg message) {
        return Optional.ofNullable(message)
                .map(SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg::getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage)
                .orElse(Collections.emptyList());
    }

    private static List<SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts> getDeviceConnectionStatuses(SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq changeRequest) {
        return Optional.ofNullable(changeRequest)
                .map(SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq::getDeviceConnectionStatus)
                .orElse(Collections.emptyList());
    }

    private static Optional<String> getDeviceId(SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts status) {
        return Optional.ofNullable(status)
                .map(SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}