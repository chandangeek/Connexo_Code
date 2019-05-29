/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentRequestConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentrequest.outbound.provider",
        service = {MeterReadingDocumentRequestConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentRequestConfirmation.SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION})
public class MeterReadingDocumentCreateConfirmationProvider implements MeterReadingDocumentRequestConfirmation, OutboundSoapEndPointProvider {

    private final Map<String, SmartMeterMeterReadingDocumentERPCreateConfirmationEOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART)
        );
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPCreateConfirmationEOut.class;
    }

    @Override
    public void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().smartMeterMeterReadingDocumentERPCreateConfirmationEOut(confirmationMessage.getConfirmationMessage());
    }
}