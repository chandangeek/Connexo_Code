/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceAplication;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.statuschangerequest.outbound.provider",
        service = {StatusChangeRequestCreateConfirmation.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + StatusChangeRequestCreateConfirmation.SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION})
public class StatusChangeRequestCreateConfirmationProvider implements StatusChangeRequestCreateConfirmation,
        OutboundSoapEndPointProvider, WebServiceAplication {

    private final Map<String, SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut> ports =
            new HashMap<>();

    private volatile Thesaurus thesaurus;

    public StatusChangeRequestCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut port,
            Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Override
    public Service get() {
        return new SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART));
    }

    @Override
    public Class getService() {
        return SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut.class;
    }

    @Override
    public void call(StatusChangeRequestCreateConfirmationMessage confirmationMessage) {
        Optional.ofNullable(ports.get(confirmationMessage.getUrl()))
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS))
                .smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
                        confirmationMessage.getConfirmationMessage());
    }

    @Override
    public String getApplication(){
        return WebServiceAplication.WebServiceApplicationName.MULTISENSE.getName();
    }
}