/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.statuschangerequest.outbound.provider",
        service = {StatusChangeRequestCreateConfirmation.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + StatusChangeRequestCreateConfirmation.SAP_STATUS_CHANGE_REQUEST_CREATE_CONFIRMATION})
public class StatusChangeRequestCreateConfirmationProvider implements StatusChangeRequestCreateConfirmation,
        OutboundSoapEndPointProvider {

    private final Map<String, SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut> ports =
            new HashMap<>();

    private volatile Thesaurus thesaurus;

    public StatusChangeRequestCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port,
            Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(
            SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
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
    public void call(StatusChangeRequestCreateConfirmationMessage confirmationMessage) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut(
                confirmationMessage.getConfirmationMessage());
    }
}